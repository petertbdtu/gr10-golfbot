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

public class BLStateController extends Thread implements BlackboardListener  {
	
	public volatile int ballCollectedCount = 0;
	public volatile boolean pauseStateMachine = false;
	public volatile boolean stopStateMachine = false;
	
	public static enum State {
		DEBUG,
		PAUSE,
		
		EXPLORE,
		IS_MOVING,
		WAIT_FOR_MOVE,
		
		COLLISION_AVOIDANCE,
		
		FIND_BALL,
		VALIDATE_BALL,
		GO_TO_BALL,
		TURN_TO_BALL,
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
	
	private CommandTransmitter commandTransmitter;
	private BLCollisionDetector collisionDetector;
	private BLBallDetector ballDetector;
	private State nextState;
	private State state;
	
	private Point currentBall;
	private boolean notDone;
	private boolean lastMoveState = false;
	private LinkedList<PolarPoint> reverseQueue;
	
	private BlackboardSample bbSample;
	


	public BLStateController(CommandTransmitter commandTransmitter, BLCollisionDetector collisionDetector, State state) {
		this.commandTransmitter = commandTransmitter;
		this.collisionDetector = collisionDetector;
		this.state = state;
		reverseQueue = new LinkedList<PolarPoint>();
		notDone = true;
	}	
	
	@Override
	public void run() {
		while(notDone) {
			
			if(pauseStateMachine && state != State.IS_MOVING && state != State.IS_COLLECTING) {
				nextState = state;
				state = State.PAUSE;
			}
				
			if(!pauseStateMachine && state == State.PAUSE)
				state = nextState;
			
			// Collision detection state overruling
			//if (collisionDetector.isDetected && state != State.COLLISION_AVOIDANCE) {
			//	state = State.COLLISION_AVOIDANCE;
			//}
			
			// DEBUG
			//if(state != tempState) {
				System.out.println("STATE:  " + state);
				//tempState = state;
			//}
			
			// State switcher
			switch(state) {
			
				case EXPLORE: {
					//commandTransmitter.robotTurn(45);
					while(bbSample == null || bbSample.scan == null);
					ballDetector.findClosestBallLidar(bbSample.scan);
//					while(ball == null) {
//						ball = ballDetector.getClosestBall();
//					}
//					
//					double angle = ((double) (new Point(0,0)).angleTo(ball));
//					System.out.println("0.0: " + angle);
//					angle = ((double) (new Point(-95,0)).angleTo(ball));
//					System.out.println("-95.0: " + angle);
					
					

					
					//nextState = State.FIND_BALL;
					state = State.COMPLETED;
					break;
				}

				case WAIT_FOR_MOVE: {
					if(bbSample.isMoving)
						state = State.IS_MOVING;
					break;
				}
				
				case IS_MOVING: {
					if(!bbSample.isMoving)
						state = nextState;
					break;
				}
				
				case COLLISION_AVOIDANCE: {
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
				}
				case FIND_BALL: {
					System.out.println("Finding Ball");
					Point ball = ballDetector.findClosestBallLidar(bbSample.scan);
					System.out.println("Done Searching");
					if(ball != null) {
						collisionDetector.swapHull(false);
						
						//Heading command
						double angleBefore = ((double) (new Point(-115,0)).angleTo(ball));
						double angle = angleBefore > 0 ? (angleBefore-180) * -1 : (angleBefore + 180) * -1;
						System.out.println("Angle Calculated: " + angle);
						
						if(angle > -4 && angle < 4) {
							//Distance command
							double distance = (new Point(-155,0)).distance(ball.x, ball.y);				
							reverseQueue.addFirst(new PolarPoint(0,-distance));
							commandTransmitter.robotTravel(distance);
							nextState = State.FETCH_BALL;
							state = State.WAIT_FOR_MOVE;
						} else {
							System.out.println("BALL FOUND AT HEADING: " + angleBefore + " | " + angle);
							reverseQueue.addFirst(new PolarPoint(-angle, 0));
							commandTransmitter.robotTurn(angle);
							nextState = State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						}
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
				
//				case GO_TO_BALL: {
//					PolarPoint command = travelQueue.pop();
//					commandTransmitter.robotTravel(command.distance);
//					nextState = State.FETCH_BALL;
//					state = State.WAIT_FOR_MOVE;
//					break;
//				}
//				
//				case TURN_TO_BALL: {
//					PolarPoint command = travelQueue.pop();
//					commandTransmitter.robotTurn(command.angle);					
//					nextState = State.FIND_BALL;
//					state = State.WAIT_FOR_MOVE;
//					break;
//				}
				
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

					if(reverseQueue.isEmpty()) {
						if(ballCollectedCount >= 4) {
							nextState = State.COMPLETED;
						} else {
							nextState = State.FIND_BALL;
						}

					} else {
						nextState = State.GO_TO_STARTING_POINT;
					}

					
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
