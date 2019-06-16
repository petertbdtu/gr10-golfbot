package blackboard;

import java.util.LinkedList;

import org.opencv.imgcodecs.Imgcodecs;

import blackboard.BlackboardSample;
import communication.CommandTransmitter;
import gui.ServerGUI;
import mapping.BallDetector;
import mapping.LidarScan;
import objects.LidarSample;
import objects.Point;
import objects.PolarPoint;
import java.lang.Math;

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
		
		EXPLORE_V2,
		WALL_SAMPLE,
		WALL_CORRECTION,
		
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
	
	private ServerGUI serverGUI;
	
	private CommandTransmitter commandTransmitter;
	private BLCollisionDetector collisionDetector;
	private BallDetector ballDetector;
	private State nextState;
	private State state;
	
	private boolean notDone;
	private boolean lastMoveState = false;
	private LinkedList<PolarPoint> reverseQueue;
	
	private BlackboardSample bbSample;
	
	private LidarScan wallScan;
	private double startDist;
	


	public BLStateController(ServerGUI gui, CommandTransmitter commandTransmitter, BLCollisionDetector collisionDetector, State state) {
		this.serverGUI = gui;
		this.commandTransmitter = commandTransmitter;
		this.collisionDetector = collisionDetector;
		this.ballDetector = new BallDetector();
		this.state = state;
		reverseQueue = new LinkedList<PolarPoint>();
		notDone = true;
	}	
	
	@Override
	public void run() {
		Point ball = null;
		double ballAngle = 0.0;
		double ballDistance = 0.0;
		String curMove = "";
		byte[] curLidarImage = null;
		State tempState = State.DEBUG;
		
		while(notDone) {
			
			if(pauseStateMachine && state != State.IS_MOVING && state != State.IS_COLLECTING) {
				nextState = state;
				state = State.PAUSE;
			}
				
			if(!pauseStateMachine && state == State.PAUSE)
				state = nextState;
			
			// Collision detection state overruling
			if (collisionDetector.isDetected && state != State.COLLISION_AVOIDANCE) {
				state = State.COLLISION_AVOIDANCE;
				curMove = "AVOIDANCE";
			}
			
			// DEBUG
			if(state != tempState) {
				//System.out.println("STATE:  " + state);
				tempState = state;
			}
			
				
			// Update GUI
			serverGUI.setState(state.toString());
			if(bbSample != null) {
				serverGUI.setIsMoving(bbSample.isMoving + "");
				serverGUI.setIsCollecting(bbSample.isCollecting + "");
				if(bbSample.scan != null) {}
					//try {
						//serverGUI.setLidarScan(ballDetector.getByteArrayFromLidarScan(bbSample.scan));
					//} catch(Exception e) {
						//TODO		
					//}
			}

			serverGUI.setCollisionDetected(collisionDetector.isDetected + "");
			serverGUI.setLastMove(curMove);
			serverGUI.setBallLocation(ball != null ? String.format("[%d:%d]", ball.x, ball.y) : "null");
			serverGUI.setBallHeading((int)ballAngle + "");
			serverGUI.setBallDistance((int)ballDistance + "");
			serverGUI.setBallsCollected(ballCollectedCount + "");			

			// State switcher
			switch(state) {
			
				case EXPLORE: {
					commandTransmitter.robotTurn(45);
					curMove = "D:45";
					nextState = State.FIND_BALL;
					state = State.WAIT_FOR_MOVE;
					break;
				}

				case WAIT_FOR_MOVE: {
					if(bbSample != null && bbSample.isMoving)
						state = State.IS_MOVING;
					break;
				}
				
				case IS_MOVING: {
					if(bbSample != null && !bbSample.isMoving)
						state = nextState;
					break;
				}
				
				/*
				case EXPLORE_V2: {
					collisionDetector.swapHull(true);
					commandTransmitter.robotTravel(300.0);
					curMove = "K:30";
					nextState = State.EXPLORE_V2;
					state = State.WAIT_FOR_MOVE;
					break;
				}*/
				
				case EXPLORE_V2: {
					if (bbSample != null && bbSample.scan != null) {
						collisionDetector.swapHull(true);
						wallScan = new LidarScan(bbSample.scan);
						startDist = 0.0;
						for (LidarSample s : bbSample.scan.getSamples()) {
							if (s.angle > 88.0 && s.angle < 92.0) {
								startDist = s.distance;
								System.out.println(startDist);
								break;
							}
						}
						
						commandTransmitter.robotTravel(200.0);
						curMove = "K:20";
						nextState = State.WALL_CORRECTION;
						state = State.WAIT_FOR_MOVE;
						

						if (startDist == 0.0) {
							nextState = State.EXPLORE_V2;
						}
					}
					break;
				}
				
				case WALL_CORRECTION: {
					if (bbSample != null && bbSample.scan != null) {
						collisionDetector.swapHull(true);
						double diff = 1000.0;
						double angleDiff, currDist = 0.0;
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if (wallScanNew.scanSize() != wallScan.scanSize()) {
							for (LidarSample s : wallScanNew.getSamples()) {
								if (s.angle > 88.0 && s.angle < 92.0) {
									currDist = s.distance;
									break;
								}
							}
							diff = startDist - currDist;
							wallScan = wallScanNew;
							state = State.WALL_CORRECTION;
							angleDiff = Math.asin(diff/200.0);
							System.out.println("[Start: "+startDist+", curr: "+currDist+", angle: "+angleDiff+"]");
							if (diff != startDist && angleDiff > 5.0 && angleDiff < -5.0) {
								commandTransmitter.robotTurn(angleDiff);
								curMove = "D:"+angleDiff;
							}
							nextState = State.EXPLORE_V2;
							state = State.WAIT_FOR_MOVE;

						}
					}
					break;
				}
				
				case COLLISION_AVOIDANCE: {
					commandTransmitter.robotStop();
					while(bbSample.isMoving) System.out.print(""); // wait for stop
					commandTransmitter.robotTravel(-50);
					while(!bbSample.isMoving) System.out.print(""); // Wait for move
					while(bbSample.isMoving) System.out.print(""); // Wait for stop
					commandTransmitter.robotTurn(90);
					while(!bbSample.isMoving) System.out.print(""); // Wait for move
					while(bbSample.isMoving) System.out.print(""); // Wait for stop
					collisionDetector.isDetected = false;
					state = State.EXPLORE_V2;
					break;
				}
				case FIND_BALL: {
					ball = bbSample != null ? ballDetector.findClosestBallLidar(bbSample.scan) : null;
					if(ball != null) {
						collisionDetector.swapHull(false);
						
						//Heading command
						double angleBefore = ((double) (new Point(-115,0)).angleTo(ball));
						ballAngle = angleBefore > 0 ? (angleBefore-180) * -1 : (angleBefore + 180) * -1;
						//System.out.println("Angle Calculated: " + ballAngle);
						
						if(ballAngle > -4 && ballAngle < 4) {
							//Distance command
							ballDistance = (new Point(-155,0)).distance(ball.x, ball.y);				
							reverseQueue.addFirst(new PolarPoint(0,-ballDistance));
							commandTransmitter.robotTravel(ballDistance);
							curMove = "K:" + (int)ballDistance;
							nextState = State.FETCH_BALL;
							state = State.WAIT_FOR_MOVE;
						} else {
							//System.out.println("BALL FOUND AT HEADING: " + angleBefore + " | " + ballAngle);
							reverseQueue.addFirst(new PolarPoint(-ballAngle, 0));
							commandTransmitter.robotTurn(ballAngle);
							curMove = "D:" + (int)ballAngle;
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
				
//				case VALIDATE_BALL: {
//					LidarScan scan = bbSample.scan;
//					currentBall = ballDetector.findClosestBallLidar(scan);
//					if(currentBall != null) {
//						float heading = (new Point(0,0)).angleTo(currentBall);
//						System.out.println("BALL VALIDATED AT HEADING: " + heading);
//						if(heading < 1 && heading > -1) {
//							System.out.println("SAME BALL");
//							state = State.GO_TO_BALL;
//						} else {
//							System.out.println("FINDING NEW BALL");
//							state = State.FIND_BALL;
//						}
//					} else {
//						state = State.EXPLORE;
//					}
//					break;
//				}
				
				case FETCH_BALL: {
					curMove = "O";
					commandTransmitter.robotCollectBall();
					state = State.GO_TO_STARTING_POINT;
					break;
				}
				
				case GO_TO_STARTING_POINT: {
					PolarPoint command = reverseQueue.pop();
					
					if(command.distance != 0) {
						curMove = "K:" + (int)command.distance;
						commandTransmitter.robotTravel(command.distance);
					} else {
						curMove = "D:" + (int)command.angle;
						commandTransmitter.robotTurn(command.angle);
					}		
						
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
					if(bbSample.isCollecting)
						state = State.IS_MOVING; 
					break;
				}
					
				case IS_COLLECTING: {
					if(!bbSample.isCollecting)
						state = State.FIND_BALL; 
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
					curMove = "A";
					commandTransmitter.robotDeliverBalls();
				}
					
				case COMPLETED: {
					notDone = false;
					break;	
				}
	
				default: {
					state = State.EXPLORE_V2;
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
				//System.out.println("KØRER VI ELLER HVAD?: " + newMoveState);
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
