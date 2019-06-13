package blackboard;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;

import blackboard.BlackboardController;
import blackboard.BlackboardSample;
import communication.CommandTransmitter;
import communication.LegoReceiver;
import communication.LidarReceiver;
import mapping.LidarScan;
import objects.Point;
import objects.PolarPoint;

public class StateController extends Thread implements BlackboardListener  {
	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		StateController st = new StateController();
		if(st.initialiseObjects()) {
			st.start();
			try { st.join(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		} 
	}
	
	private enum State {
		DEBUG,
		
		EXPLORE,
		EXPLOREV2,
		EXPLOREV3,
		IS_MOVING,
		WAIT_FOR_MOVE,
		
		COLLISION_AVOIDANCE,
		
		FIND_BALL,
		VALIDATE_BALL,
		GO_TO_BALL,
		GO_TO_STARTING_POINT,
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
	private boolean lastMoveState = false;
	
	private LinkedList<PolarPoint> TravelQueue;
	private LinkedList<PolarPoint> ReverseQueue;
	
	private BlackboardSample bbSample;
	private CommandTransmitter commandTransmitter;
	private LidarReceiver lidarReceiver;
	private LegoReceiver legoReceiver;
	private BlackboardController bController;
	private BLCollisionDetector collisionDetector;
	private BLBallDetector ballDetector;

	public StateController() {
		state = State.EXPLORE;
		notDone = true;
	}
	
	PolarPoint hej;

	@Override
	public void run() {
		System.out.println("Starting State Machine");
		State tempState = State.DEBUG;
		while(notDone) {
			
			// Collision detection state overruling
			if (collisionDetector.isDetected && state != State.COLLISION_AVOIDANCE) {
				state = State.COLLISION_AVOIDANCE;
			}
			
			// DEBUG
			if(state != tempState) {
				System.out.println("STATE:  " + state);
				tempState = state;
			}
			
			// State switcher
			switch(state) {
			
				case EXPLORE:
					commandTransmitter.robotTravel(0, 700);
					nextState = State.EXPLORE;
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
					
				case COLLISION_AVOIDANCE:
					commandTransmitter.robotStop();
					while(bbSample.isMoving); // wait for stop
					commandTransmitter.robotTravel(0,-50);
					while(!bbSample.isMoving); // Wait for move
					while(bbSample.isMoving); // Wait for stop
					commandTransmitter.robotTravel(90,0);
					while(!bbSample.isMoving); // Wait for move
					while(bbSample.isMoving); // Wait for stop
					collisionDetector.isDetected = false;
					state = State.EXPLORE;
					break;

				case FIND_BALL: {
					LidarScan oldScan = new LidarScan(bbSample.scan);
					LidarScan newScan = new LidarScan();
					while(oldScan.scanSize() == newScan.scanSize()) {
						newScan = new LidarScan(bbSample.scan);
					}
					Point ball = ballDetector.findClosestBallLidar(newScan);
					if(ball != null) {
						//Heading command
						double heading = (double) (new Point(-120,0)).angleTo(currentBall);
						TravelQueue.addLast(new PolarPoint(heading,0));
						ReverseQueue.addFirst(new PolarPoint(-heading, 0));
						
						//Distance command
						double distance = (new Point(0,0)).distance(ball.x, ball.y);
						TravelQueue.addLast(new PolarPoint(0,distance));
						ReverseQueue.addFirst(new PolarPoint(0,-distance));
						
						state = State.GO_TO_BALL;
					} else {
						// Keep exploring
						state = nextState;
					}
					break;
				}
				case VALIDATE_BALL: {
					LidarScan scan = bbSample.scan;
					currentBall = ballDetector.findClosestBallLidar(scan);
					if(currentBall != null) {
						float heading = (new Point(0,0)).angleTo(currentBall);
						System.out.println("BALL VALIDATED AT HEADING: " + heading);
						if(heading < 1 && heading > -1) {
							System.out.println("SAME BALL");
							state = State.GO_TO_BALL;
						} else {
							System.out.println("FINDING NEW BALL");
							state = State.FIND_BALL;
						}
					} else {
						state = State.EXPLORE;
					}
					break;
				}
				
				case GO_TO_BALL: {
					PolarPoint command = TravelQueue.pop();
					commandTransmitter.robotTravel(command.angle, command.distance);
					if(TravelQueue.isEmpty())
						nextState = State.FETCH_BALL;
					else
						nextState = State.GO_TO_BALL;
					state = State.WAIT_FOR_MOVE;
				}
				
				case FETCH_BALL: {
					// TODO!!!!
					//commandTransmitter.robotCollectBall();
					
					state = State.COMPLETED;
					break;
				}
				
				case GO_TO_STARTING_POINT: {
					PolarPoint command = ReverseQueue.pop();
					commandTransmitter.robotTravel(command.angle, command.distance);
					if(TravelQueue.isEmpty())
						nextState = State.EXPLORE;
					else
						nextState = State.GO_TO_STARTING_POINT;
					state = State.WAIT_FOR_MOVE;
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

	private LidarScan tempScan;
	private LidarScan oldScan;

	
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = new BlackboardSample(bbSample);
		if(bbSample != null) {
			boolean newMoveState = bbSample.isMoving;
			if(lastMoveState != newMoveState) {
				System.out.println("KØRER VI ELLER HVAD?: " + newMoveState);
				lastMoveState = newMoveState;
			}
		}
//		tempScan = bbSample.scan;
//		if(tempScan != null) {
//			if(oldScan == null) {
//				try {
//					Imgcodecs.imwrite("testScan" + bbSample.cycle + ".png", ballDetector.scanToMap(tempScan));
//			        FileOutputStream fileOut = new FileOutputStream("testScan" + bbSample.cycle + ".data");
//			        ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
//			        objectOut.writeObject(bbSample.scan);
//			        objectOut.close();
//				} catch (Exception ex) {
//		            System.out.println("The Object could not be written to a file");
//		        }
//				oldScan = new LidarScan(tempScan);
//			} else {
//				if(tempScan.scanSize() != oldScan.scanSize()) {
//					try {
//						Imgcodecs.imwrite("testScan" + bbSample.cycle + ".png", ballDetector.scanToMap(tempScan));
//				        FileOutputStream fileOut = new FileOutputStream("testScan" + bbSample.cycle + ".data");
//				        ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
//				        objectOut.writeObject(bbSample.scan);
//				        objectOut.close();
//					} catch (Exception ex) {
//			            System.out.println("The Object could not be written to a file");
//			        }
//					oldScan = new LidarScan(tempScan);
//				}
//			}
//		}
	}
	
	private void saveScan(LidarScan scan) {
		Imgcodecs.imwrite("Scanning.png", ballDetector.scanToMap(scan));
	}
}
