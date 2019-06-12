package blackboard;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;

import blackboard.BlackboardController;
import blackboard.BlackboardSample;
import communication.CommandTransmitter;
import communication.LegoReceiver;
import communication.LidarReceiver;
import mapping.LidarScan;
import objects.Point;

public class StateControllerTemp extends Thread implements BlackboardListener  {
	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		StateControllerTemp st = new StateControllerTemp();
		if(st.initialiseObjects()) {
			st.start();
			try { st.join(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		} 
	}
	
	private enum State {
		DEBUG,
		
		EXPLORE,
		IS_MOVING,
		WAIT_FOR_MOVE,
		
		COLLISION_AVOIDANCE_TURN,
		COLLISION_AVOIDANCE_REVERSE,
		COLLISION_AVOIDANCE_STOP,
		
		FIND_BALL,
		VALIDATE_BALL,
		GO_TO_BALL,
		FETCH_BALL,
		WAIT_FOR_COLLECT,
		IS_COLLECTING,

		FIND_GOAL,
		VALIDATE_GOAL,
		GO_TO_GOAL,
		DELIVER_BALLS,
		
		COMPLETED
	}
	
	private State nextState;
	private State state;
	
	private Point currentBall;
	private boolean notDone;

	private BlackboardSample bbSample;
	private CommandTransmitter commandTransmitter;
	private LidarReceiver lidarReceiver;
	private LegoReceiver legoReceiver;
	private BlackboardController bController;
	private BLCollisionDetector collisionDetector;
	private BLBallDetector ballDetector;

	public StateControllerTemp() {
		state = State.EXPLORE;
		notDone = true;
	}

	@Override
	public void run() {
		System.out.println("Starting State Machine");
		boolean lastMoveState = false;
		State tempState = State.DEBUG;
		while(notDone) {
			
			if(bbSample != null) {
				boolean newMoveState = bbSample.isMoving;
				if(lastMoveState != newMoveState) {
					System.out.println("KØRER VI ELLER HVAD?: " + newMoveState);
					lastMoveState = newMoveState;
				}
			}
			
			// DEBUG
			if(state != tempState) {
				System.out.println("STATE:  " + state);
				tempState = state;
			}
			
			// Collision detection state overruling
			if (collisionDetector.getIsDetected())
				state = State.COLLISION_AVOIDANCE_STOP;
			
			// State switcher
			switch(state) {
			
				case EXPLORE:
					commandTransmitter.robotTravel(0, -1000);
					nextState = State.FIND_BALL;
					state = State.WAIT_FOR_MOVE;
					break;
					
				case WAIT_FOR_MOVE:
					if(bbSample.isMoving)
						state = State.IS_MOVING;
					break;
					
				case IS_MOVING:
					if(!bbSample.isMoving)
						state = nextState;
					break;
					
				case COLLISION_AVOIDANCE_STOP:
					commandTransmitter.robotStop();
					nextState = State.COLLISION_AVOIDANCE_REVERSE;
					state = State.IS_MOVING;
					break;
				case COLLISION_AVOIDANCE_REVERSE:
					commandTransmitter.robotTravel(0,50);
					nextState = State.COLLISION_AVOIDANCE_TURN;
					state = State.WAIT_FOR_MOVE;
					break;
				case COLLISION_AVOIDANCE_TURN:
					commandTransmitter.robotTravel(90,0);
					collisionDetector.setIsDetected(false);
					nextState = State.EXPLORE;
					state = State.WAIT_FOR_MOVE;
					break;
				
				case FIND_BALL: {
					LidarScan scan = bbSample.scan;
					Point p = ballDetector.findClosestBallLidar(scan);
					if(p != null) {
						float heading = (new Point(0,0)).angleTo(p);
						commandTransmitter.robotTravel(heading, 0);
						nextState = State.VALIDATE_BALL;
						state = State.WAIT_FOR_MOVE;
					} else {
						// Keep exploring
						state = State.EXPLORE;
					}
					break;
				}
				case VALIDATE_BALL: {
					LidarScan scan = bbSample.scan;
					currentBall = ballDetector.findClosestBallLidar(scan);
					if(currentBall != null) {
						float heading = (new Point(0,0)).angleTo(currentBall);
						if(heading < 1) {
							state = State.GO_TO_BALL;
						} else {
							state = State.FIND_BALL;
						}
					} else {
						state = State.EXPLORE;
					}
					break;
				}
				
				case GO_TO_BALL : {
					double distance = (new Point(0,0)).distance(currentBall.x, currentBall.y);
					commandTransmitter.robotTravel(0, distance);
					state = State.WAIT_FOR_MOVE;
					nextState = State.FETCH_BALL;
				}
				
				case FETCH_BALL: {
					// TODO!!!!
					//commandTransmitter.robotCollectBall();
					
					state = State.COMPLETED;
					break;
				}
				
				case WAIT_FOR_COLLECT: {
					if(bbSample.isCollecting) { state = State.IS_MOVING; }
					break;
				}
					
				case IS_COLLECTING: {
					if(!bbSample.isCollecting) { 
						state = State.FIND_BALL; 
					}
					break;
				}
				
				case FIND_GOAL: {
					// TODO IMPLEMENT
					break;
				}
					
				case VALIDATE_GOAL: {
					// TODO IMPLEMENT
					break;
				}

				case GO_TO_GOAL: {
					// TODO IMPLEMENT
					break;
				}
					
				case DELIVER_BALLS: {
					// TODO IMPLEMENT
				}
					
				case COMPLETED:
					notDone = false;
					break;	
					
				default:
					state = State.EXPLORE;
					break;
			}
		}
	}

	public boolean initialiseObjects() {
		// Build Lidar receiver
		System.out.println("Building Lidar Receiver...");
		lidarReceiver = new LidarReceiver();
		if(lidarReceiver.bindSocket(5000)) {
			lidarReceiver.start();
			System.out.println("Lidar Receiver succes");
		} else {
			System.out.println("Lidar Receiver failed");
			return false;
		}

		// Build Lego Receiver
		System.out.println("Building Lego Receiver...");
		legoReceiver = new LegoReceiver();
		if(legoReceiver.connect(3000)) {  //connect(3000, 3001, 3002)
			legoReceiver.start();
			System.out.println("Lego Receiver succes");
		} else {
			System.out.println("Lego Receiver failed");
			return false;
		}

		// Command Transmitter
		System.out.println("Building Command Transmitter...");
		commandTransmitter = new CommandTransmitter();
		if(commandTransmitter.connect(3001)) {
			System.out.println("Command Transmitter succes");
		} else {
			System.out.println("Command Transmitter failed");
			return false;
		}
		
		// Collision Detection
		System.out.println("Building Collision Detector...");
		collisionDetector = new BLCollisionDetector();
		collisionDetector.start();
		System.out.println("Collision detection activated");
	
		// Ball Detector
		System.out.println("Building Ball Detector...");
		ballDetector = new BLBallDetector();
		System.out.println("Ball detection activated");

		// Blackboard Controller
		System.out.println("Building blackboard...");
		bController = new BlackboardController(null, legoReceiver, lidarReceiver);
		bController.registerListener(commandTransmitter);
		bController.registerListener(collisionDetector);
		bController.registerListener(this);
		bController.start();
		System.out.println("Blackboard succes");
		
		return true;
	}

	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = new BlackboardSample(bbSample);
		try {
			Imgcodecs.imwrite("testScan" + bbSample.cycle + ".png", ballDetector.getMap(bbSample.scan));
	        FileOutputStream fileOut = new FileOutputStream("testScan" + bbSample.cycle + ".data");
	        ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
	        objectOut.writeObject(bbSample.scan);
	        objectOut.close();
		} catch (Exception ex) {
            System.out.println("The Object could not be written to a file");
        }
		
	}
}
