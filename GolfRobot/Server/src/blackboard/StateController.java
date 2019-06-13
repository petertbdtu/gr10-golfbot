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
import objects.LidarSample;
import objects.Point;
import objects.PolarPoint;
import test.BallDetectorTest;

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
	
	private LinkedList<PolarPoint> travelQueue;
	private LinkedList<PolarPoint> reverseQueue;
	
	private BlackboardSample bbSample;
	private CommandTransmitter commandTransmitter;
	private LidarReceiver lidarReceiver;
	private LegoReceiver legoReceiver;
	private BlackboardController bController;
	private BLCollisionDetector collisionDetector;
	private BLBallDetector ballDetector;

	public StateController() {
		state = State.EXPLORE;
		travelQueue = new LinkedList<PolarPoint>();
		reverseQueue = new LinkedList<PolarPoint>();
		notDone = true;
	}
	
	PolarPoint hej;
	private int count = 0;
	
	
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
			
				case EXPLORE: {
					commandTransmitter.robotTurn(50);
					nextState = State.FIND_BALL;
					state = State.WAIT_FOR_MOVE;
					break;
				}

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
					commandTransmitter.robotTravel(-50);
					while(!bbSample.isMoving); // Wait for move
					while(bbSample.isMoving); // Wait for stop
					commandTransmitter.robotTurn(90);
					while(!bbSample.isMoving); // Wait for move
					while(bbSample.isMoving); // Wait for stop
					collisionDetector.isDetected = false;
					state = State.EXPLORE;
					break;

				case FIND_BALL: {
					Point ball = ballDetector.getClosestBall();
					if(ball != null) {
						collisionDetector.swapHull(false);
						
						//Heading command
						double heading = ((double) (new Point(-95,0)).angleTo(ball)) - 180;
						travelQueue.add(new PolarPoint(heading,0));
						reverseQueue.addFirst(new PolarPoint(-heading, 0));
						
						//Distance command
						double distance = (new Point(0,0)).distance(ball.x, ball.y);
						travelQueue.add(new PolarPoint(0,distance));
						reverseQueue.addFirst(new PolarPoint(0,-distance));
						
//						saveScan(bbSample.scan);
						state = State.GO_TO_BALL;
					} else {
						collisionDetector.swapHull(true);
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
					PolarPoint command = travelQueue.pop();
					
					if(command.distance != 0)
						commandTransmitter.robotTravel(command.distance);
					else
						commandTransmitter.robotTurn(command.angle);					
					
					if(travelQueue.isEmpty())
						nextState = State.FETCH_BALL;
					else
						nextState = State.GO_TO_BALL;					
					
					state = State.WAIT_FOR_MOVE;
					break;
					
				}
				
				case FETCH_BALL: {
					commandTransmitter.robotCollectBall();
					state = State.GO_TO_STARTING_POINT;
					break;
				}
				
				case GO_TO_STARTING_POINT: {
					PolarPoint command = reverseQueue.pop();
					
					if(command.distance != 0)
						commandTransmitter.robotTravel(command.distance);
					else
						commandTransmitter.robotTurn(command.angle);

					if(travelQueue.isEmpty())
						nextState = State.EXPLORE;
					else
						nextState = State.GO_TO_STARTING_POINT;
					
					state = State.WAIT_FOR_MOVE;
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
					commandTransmitter.robotDeliverBalls();
				}
					
				case COMPLETED: {
					notDone = false;
					break;	
				}
	
				default: {
					state = State.EXPLORE;
					break;
				}
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
		ballDetector.start();
		System.out.println("Ball detection activated");

		// Blackboard Controller
		System.out.println("Building blackboard...");
		bController = new BlackboardController(null, legoReceiver, lidarReceiver);
		bController.registerListener(commandTransmitter);
		bController.registerListener(collisionDetector);
		bController.registerListener(ballDetector);
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
		System.out.println("Scans;");
		for(LidarSample sample : scan.getSamples()) {
			System.out.printf("[%3.2f] [%4.2f]\n", sample.angle, sample.distance);
		}
		Imgcodecs.imwrite("Scanning.png", ballDetector.scanToMap(scan));
	}
}
