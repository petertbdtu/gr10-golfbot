package blackboard;

import java.util.LinkedList;


import blackboard.BlackboardSample;
import communication.CommandTransmitter;
import gui.ServerGUI;
import mapping.LidarScan;
import objects.LidarSample;
import objects.Point;
import objects.PolarPoint;

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
		
		FOLLOW_WALL,
		WALL_CORRECTION,
		
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
	private State nextState;
	private State state;
	
	private boolean notDone;
	private LinkedList<PolarPoint> reverseQueue;
	
	private volatile BlackboardSample bbSample;
	
	public BLStateController(ServerGUI gui, CommandTransmitter commandTransmitter, BLCollisionDetector collisionDetector, State state) {
		this.serverGUI = gui;
		this.commandTransmitter = commandTransmitter;
		this.collisionDetector = collisionDetector;
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
		State tempState = State.DEBUG;
		LidarScan wallScanOld = new LidarScan();
		int startDist = 0;
		
		while(notDone) {
			
			if(pauseStateMachine && state != State.IS_MOVING && state != State.IS_COLLECTING) {
				nextState = state;
				state = State.PAUSE;
			}
				
			if(!pauseStateMachine && state == State.PAUSE)
				state = nextState;
			
			// Collision detection state overruling
			if (collisionDetector.isDetected() && state != State.COLLISION_AVOIDANCE) {
				state = State.COLLISION_AVOIDANCE;
				curMove = "AVOIDANCE";
			}
			
			// DEBUG
			if(state != tempState) {
				System.out.println("STATE:  " + state);
				tempState = state;
			}
			
				
			// Update GUI
			serverGUI.setState(state.toString());
			if(bbSample != null) {
				serverGUI.setIsMoving(bbSample.isMoving + "");
				serverGUI.setIsCollecting(bbSample.isCollecting + "");
			}

			serverGUI.setCollisionDetected(collisionDetector.isDetected() + "");
			serverGUI.setLastMove(curMove);
			//serverGUI.setBallLocation(ball != null ? String.format("[%d:%d]", ball.x, ball.y) : "null");
			//serverGUI.setBallHeading((int)ballAngle + "");
			//serverGUI.setBallDistance((int)ballDistance + "");
			serverGUI.setBallsCollected(ballCollectedCount + "");			

			// State switcher
			switch(state) {
			
				case EXPLORE: {
					commandTransmitter.robotTurn(45);
					curMove = "D:45";
					nextState = State.EXPLORE;
					state = State.WAIT_FOR_MOVE;
					break;
				}
				
				case FOLLOW_WALL: {
					startDist = 0;
					if (bbSample != null && bbSample.scan != null) {
						//System.out.println("Found sample");
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						//System.out.println("Sweet scan info: " + wallScanNew.scanSize() + ":" + wallScanOld.scanSize());
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) {
							//System.out.println("Found new scan");
							for (LidarSample s : wallScanNew.getSamples()) {
								if (s.angle > 268.0 && s.angle < 272.0) {
									startDist = (int)s.distance;
									break;
								}
							}
							wallScanOld = wallScanNew;
						}
					}
					if(startDist != 0) {
						collisionDetector.swapHull(true);
						commandTransmitter.robotTravel(200.0);
						curMove = "K:200";
						nextState = State.WALL_CORRECTION;
						state = State.WAIT_FOR_MOVE;
					}
					break;
				}
				
				case WALL_CORRECTION: {
					int endDist = 0;
					if (bbSample != null && bbSample.scan != null) {
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) {
							for (LidarSample s : wallScanNew.getSamples()) {
								if (s.angle > 268.0 && s.angle < 272.0) {
									endDist = (int)s.distance;
									break;
								}
							}
							wallScanOld = wallScanNew;
						}
					}
					if(endDist != 0) {
						double diff = startDist - endDist;
						double angleDiff = Math.asin(diff/200.0);
						System.out.println("[Start: "+startDist+", curr: "+endDist+", angle: "+angleDiff+"]");
						if (angleDiff > 5.0 && angleDiff < -5.0) {
							commandTransmitter.robotTurn(angleDiff);
							curMove = "D:"+angleDiff;
							nextState = State.FOLLOW_WALL;
							state = State.WAIT_FOR_MOVE;
						} else {
							state = State.FOLLOW_WALL;
						}
					}
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
				
				case COLLISION_AVOIDANCE: {
					commandTransmitter.robotStop();
					while(bbSample.isMoving); // wait for stop
					commandTransmitter.robotTravel(-50);
					while(!bbSample.isMoving); // Wait for move
					while(bbSample.isMoving); // Wait for stop
					commandTransmitter.robotTurn(90);
					while(!bbSample.isMoving); // Wait for move
					while(bbSample.isMoving); // Wait for stop
					collisionDetector.setDetected(false);
					state = State.FOLLOW_WALL;
					break;
				}
				case FIND_BALL: {
					ball = (bbSample != null && bbSample.balls.size() > 0) ? bbSample.balls.get(0) : null;
					if(ball != null) {
						collisionDetector.swapHull(false);
						
						//Heading command
						double angleBefore = ((double) (new Point(-115,0)).angleTo(ball));
						ballAngle = angleBefore > 0 ? (angleBefore-180) * -1 : (angleBefore + 180) * -1;
						System.out.println("Angle Calculated: " + ballAngle);
						
						if(ballAngle > -4 && ballAngle < 4) {
							//Distance command
							ballDistance = (new Point(-155,0)).distance(ball.x, ball.y);				
							reverseQueue.addFirst(new PolarPoint(0,-ballDistance));
							commandTransmitter.robotTravel(ballDistance);
							curMove = "K:" + (int)ballDistance;
							nextState = State.FETCH_BALL;
							state = State.WAIT_FOR_MOVE;
						} else {
							System.out.println("BALL FOUND AT HEADING: " + angleBefore + " | " + ballAngle);
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
				
				case FETCH_BALL: {
					curMove = "O";
					commandTransmitter.robotCollectBall();
					ballCollectedCount++;
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
					state = State.EXPLORE;
					break;
				}
			}
		}
	}
	
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = new BlackboardSample(bbSample);
	}
}
