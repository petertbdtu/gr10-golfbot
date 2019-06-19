package blackboard;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import blackboard.BlackboardSample;
import communication.CommandTransmitter;
import gui.ServerGUI;
import mapping.LidarScan;
import mapping.Vision;
import objects.LidarSample;
import objects.Point;
import objects.PolarPoint;

public class BLStateController extends Thread implements BlackboardListener  {
	
	public volatile int ballCollectedCount = -1;
	public volatile boolean pauseStateMachine = false;
	public volatile boolean stopStateMachine = false;
	private volatile BlackboardSample bbSample;

	private final int MAX_BALL_DISTANCE = 300;
	private final int FOLLOW_WALL_STEPSIZE = 200;
	private final int LIDAR_TO_FRONT_LENGTH = 380;
	private final int LIDAR_TO_RIGHT_LENGTH = 260;
	private final int BALL_VALIDATION_MAX_COUNT = 3;
	private final int BALL_FINDING_MAX_COUNT = 3;
	private final Point ORIGIN_WHEEL = new Point(107,0);
	private final Point ORIGIN_TUBE = new Point(155,0);
	
	public static enum State {
		DEBUG,
		PAUSE,
		
		EXPLORE,
		IS_MOVING,
		WAIT_FOR_MOVE,
		
		COLLISION_AVOIDANCE,
		
		FOLLOW_WALL,
		WALL_CORRECTION,
		WALL_CORRECTION_TURNIN,
		WALL_CORRECTION_TURNSTRAIGHT,
		WALL_CORRECTION_TRAVEL,
		WALL_CORRECTION_TURNBACK,
		
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
	private State nextNextState;
	private State nextState;
	private State state;
	
	private boolean notDone;
	private double reverseAngle;
	private double reverseDistance;
	
	
	public BLStateController(ServerGUI gui, CommandTransmitter commandTransmitter, State state) {
		this.serverGUI = gui;
		this.commandTransmitter = commandTransmitter;
		this.state = state;
		notDone = true;
	}	
	
	@Override
	public void run() {
		String curMove = "";
		LidarScan wallScanOld = new LidarScan();
		LidarScan ballScanOld = new LidarScan();
		boolean followWallTurnState = false;
		double lengthToCorrect = 0;
		double lengthToTravel = 0;
		double angleToCorrectIn = 0;
		double angleToCorrectBack = 0;
		int ballValidationCount = 0;
		int ballFindingCount = 0;
		State tempState = State.DEBUG;
		incrementBallsFetched();
		
		while(notDone) {
			
			// Pause
			
			if(state != tempState && state != State.IS_MOVING && state != State.WAIT_FOR_MOVE) {
				tempState = state;
				System.out.println("STATE: " + tempState);
			}	
			
			// Pause
			if(pauseStateMachine && state != State.IS_MOVING && state != State.IS_COLLECTING && state != State.PAUSE) {
				nextState = state;
				state = State.PAUSE;
			}			
			
			// Update GUI
			if(state != State.IS_MOVING && state != State.WAIT_FOR_MOVE)
				serverGUI.setState(state.toString());
			serverGUI.setLastMove(curMove);
			//serverGUI.setBallLocation(ball != null ? String.format("[%d:%d]", ball.x, ball.y) : "null");
			//serverGUI.setBallHeading((int)ballAngle + "");
			//serverGUI.setBallDistance((int)ballDistance + "");

			// State switcher
			switch(state) {
			
				case PAUSE: {
					if(!pauseStateMachine) {
						state = nextState;
					} 
					break;
				}
				

				/*
				 * State called with every move, to ensure command was received by robot
				 */
				case WAIT_FOR_MOVE: {
//					System.out.println("Entering WAIT FOR MOVE");
					if(bbSample != null && bbSample.isMoving) {
						System.out.println("IF bbSample not null");
						state = State.IS_MOVING;
					}
//					System.out.println("LEAVING WAIT FOR MOVE");
					break;
				}
				
				/*
				 * State called with every move, to ensure command was finished by robot
				 */
				case IS_MOVING: {
//					System.out.println("Entering IS MOVING");
					if(bbSample != null && !bbSample.isMoving) {
						System.out.println("If bbSample not null");
						state = nextState;
					}
//					System.out.println("Leaving IS MOVING");
					break;
				}
				
				/*
				 * State where the robot follows the right wall
				 */
				case FOLLOW_WALL: {
					System.out.println("Entering FOLLOW WALL");
					int distForwardMax = 0;
					// Scan found
					if (bbSample != null && bbSample.scan != null) {
						System.out.println("IF bbSample not null");
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						// New Scan
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) {
							System.out.println("If new scan is not old scan");
							for (LidarSample sample : wallScanNew.getSamples()) {
								// Infront of Robot & max distForward
								if (sample.angle > 175.0 && sample.angle < 185.0 && sample.distance > distForwardMax) {
									System.out.println("If angle found between 175-185");
									distForwardMax = (int) sample.distance;
								}
							}
							wallScanOld = wallScanNew;
						}
					}
					if(distForwardMax != 0) {
						System.out.println("If distForwardMax not 0");
						if(followWallTurnState || distForwardMax <= LIDAR_TO_FRONT_LENGTH) { // TURN
							System.out.println("If dist <= LIDAR TO FRONT LENGTH");
							followWallTurnState = !followWallTurnState;
							commandTransmitter.robotTurn(45.0);
							curMove = "D:90.0";
							nextState = State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						} else if(distForwardMax <= LIDAR_TO_FRONT_LENGTH + FOLLOW_WALL_STEPSIZE) { // SMALL STEP
							System.out.println("If dist <= LIDAR FRONT LENGTH + STEPSIZE");
							int dist = distForwardMax - LIDAR_TO_FRONT_LENGTH;
							commandTransmitter.robotTravel(dist);
							curMove = "K:" + dist;
							nextState = State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						} else { // FULL STEP
							System.out.println("If dist too large");
							int dist = FOLLOW_WALL_STEPSIZE;
							commandTransmitter.robotTravel(dist);
							curMove = "K:" + dist;
							nextState = State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						}
					}
					System.out.println("Leaving FOLLOW WALL");
					break;
				}
				
				/*
				 * State where the angle driving is corrected corresponding to the right wall
				 */
				case WALL_CORRECTION: {
					System.out.println("Entering WALL CORRECTION");
					int distRightMaxEnd = Integer.MAX_VALUE;
					if (bbSample != null && bbSample.scan != null) { // Scan present in BBSample
						System.out.println("If bbSample not null");
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) { // Scan in BBSample is new
							System.out.println("If sample is new");
							for (LidarSample s : wallScanNew.getSamples()) {
								if (s.angle > 88.0 && s.angle < 115.0 && s.distance < distRightMaxEnd) { // Points found to the right
									distRightMaxEnd = (int) s.distance;
									System.out.println("if angle between 88-92");
								}
							}
							wallScanOld = wallScanNew;
							
							if(distRightMaxEnd != 0 && distRightMaxEnd < 500) {		
								System.out.println("if distRightMax not 0 and < 500");
								lengthToCorrect = distRightMaxEnd - LIDAR_TO_RIGHT_LENGTH;
								angleToCorrectIn = (Math.toDegrees(Math.asin(lengthToCorrect/FOLLOW_WALL_STEPSIZE))) * -1;
								if((angleToCorrectIn < -5 || angleToCorrectIn > 5) && distRightMaxEnd < 500) {
									System.out.println("If angleCorrect between -5 and 5");
									state = State.WALL_CORRECTION_TURNSTRAIGHT;
								} else {
									System.out.println("if angleCorrect not between -5 and 5");
									state = State.FOLLOW_WALL;
								}
							} else {
								System.out.println("if distRightMax 0 or > 500");
								state = State.FOLLOW_WALL;
							}
						}
					}
					System.out.println("Leaving WALL CORRECTION");
					break;
				}
				
				case WALL_CORRECTION_TURNSTRAIGHT: {
					System.out.println("Entering WALL CORRECTION STRAIGHT");
					curMove = "D:" + angleToCorrectIn;
					commandTransmitter.robotTurn(angleToCorrectIn);
					nextState = State.WALL_CORRECTION_TURNIN;
					state = State.WAIT_FOR_MOVE;
					System.out.println("Leaving WALL CORRECTION STRAIGHT");
					break;
				}
				
				case WALL_CORRECTION_TURNIN: {
					System.out.println("Entering WALL CORRECTION TURNIN");
					double distForwardMax = 0;
					// Scan found
					if (bbSample != null && bbSample.scan != null) {
						System.out.println("If bbSample not null");
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						// New Scan
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) {
							System.out.println("If scan is new");
							
							for (LidarSample sample : wallScanNew.getSamples()) {
								// Infront of Robot & max distForward
								if (sample.angle > 175.0 && sample.angle < 185.0 && sample.distance > distForwardMax) {
									System.out.println("If angle between 175-185");
									distForwardMax = sample.distance;
								}
							}
							wallScanOld = wallScanNew;						
						}
					}
					if(distForwardMax != 0) {
						System.out.println("If distForwardMax not 0");
						double distForward = distForwardMax > FOLLOW_WALL_STEPSIZE ? FOLLOW_WALL_STEPSIZE / 3.0 : distForwardMax / 3.0;
						angleToCorrectBack = (Math.toDegrees(Math.atan(lengthToCorrect / distForward))) * -1;
						lengthToTravel = Math.sqrt(Math.pow(lengthToCorrect, 2) + Math.pow(distForward, 2));
						curMove = "D:" + angleToCorrectBack;
						commandTransmitter.robotTurn(angleToCorrectBack);
						nextState = State.WALL_CORRECTION_TRAVEL;
						state = State.WAIT_FOR_MOVE;
					}
					System.out.println("Leaving WALL CORRECTION TURNIN");
					break;
				}
				
				case WALL_CORRECTION_TRAVEL: {
					System.out.println("Entering WALL CORRECTION TRAVEL");
					curMove = "K:" + lengthToTravel;
					commandTransmitter.robotTravel(lengthToTravel);
					nextState = State.WALL_CORRECTION_TURNBACK;
					state = State.WAIT_FOR_MOVE;
					System.out.println("Leaving WALL CORRECTION TRAVEL");
					break;
				}
				
				case WALL_CORRECTION_TURNBACK: {
					System.out.println("Entering WALL CORRECTION TURNBACK");
					curMove = "D:" + angleToCorrectBack * -1;
					commandTransmitter.robotTurn(angleToCorrectBack * -1);
					nextState = State.FOLLOW_WALL;
					state = State.WAIT_FOR_MOVE;
					System.out.println("Leaving WALL CORRECTION TURNBACK");
					break;
				}

				case FIND_BALL:{
					System.out.println("Entering FIND BALL");
					if(ballFindingCount >= BALL_FINDING_MAX_COUNT) {
						System.out.println("If found max number of balls");
						if(followWallTurnState)
							nextNextState = State.FOLLOW_WALL;
						else
							nextNextState = State.WALL_CORRECTION;
						state = State.GO_TO_STARTING_POINT;
						ballFindingCount = 0;
					} else if(bbSample != null && bbSample.scan != null) {
						System.out.println("If sample not null");
						LidarScan ballScanNew = new LidarScan(bbSample.scan);
						// New Scan
						if(ballScanNew.scanSize() != ballScanOld.scanSize()) {
							System.out.println("If scan is new");
							Point nearestBall = getNearestBall(ballScanNew);
							ballFindingCount++;
							
							if(nearestBall != null){
								System.out.println("If nearestBall not null");
								double ballAngle = ORIGIN_WHEEL.angleTo(nearestBall);
								if((ballAngle > 70 || ballAngle < -70) && ORIGIN_WHEEL.distance(nearestBall) < MAX_BALL_DISTANCE) {
									serverGUI.setBallHeading((int) ballAngle + "");
									reverseAngle += -ballAngle;
									commandTransmitter.robotTurn(ballAngle);
									curMove = "D:" + (int)ballAngle;
									nextState = State.VALIDATE_BALL;
									state = State.WAIT_FOR_MOVE;
								}
							} 
							
							ballScanOld = ballScanNew;						
						}			
						
					}
					System.out.println("Leaving FIND BALL");
					break;
				}
				
				case VALIDATE_BALL: {
					System.out.println("Entering VALIDATE BALL");
					if(ballValidationCount >= BALL_VALIDATION_MAX_COUNT) {
						System.out.println("If validated max");
						ballValidationCount = 0;
						state = State.GO_TO_STARTING_POINT;
						nextNextState = State.FIND_BALL;
					} else if(bbSample != null && bbSample.scan != null) { // FOUND SCAN
						System.out.println("If bbSample not null");
						LidarScan ballScanNew = new LidarScan(bbSample.scan);
						if(ballScanNew.scanSize() != ballScanOld.scanSize()) { // NEW SCAN
							System.out.println("If scan is new");
							Point nearestBall = getNearestBallFront(ballScanNew);
							if(nearestBall != null) {
								System.out.println("If nearestBall not null");
								double ballAngle = ORIGIN_WHEEL.angleTo(nearestBall);
								serverGUI.setBallHeading((int) ballAngle + "");
								if(ballAngle > -3 && ballAngle < 3) { //Distance command
									System.out.println("If ballAngle between -4 and 4");
									ballValidationCount = 0;
									double ballDistance = ORIGIN_TUBE.distance(nearestBall.x, nearestBall.y);	
									System.out.println((int) ballDistance + "");
									reverseDistance += -ballDistance;
									commandTransmitter.robotTravel(ballDistance);
									curMove = "K:" + (int)ballDistance;
									nextState = State.FETCH_BALL;
									state = State.WAIT_FOR_MOVE;
								} else { //Heading command
									System.out.println("If ballAngle not between -4 and 4");
									ballValidationCount++;
									reverseAngle += -ballAngle;
									commandTransmitter.robotTurn(ballAngle);
									curMove = "D:" + (int)ballAngle;
									nextState = State.VALIDATE_BALL;
									state = State.WAIT_FOR_MOVE;
								}
							} else {
								System.out.println("If ball not found");
								state = State.FIND_BALL;
								ballValidationCount = 0;
							}
						}
					
					}
					System.out.println("Leaving VALIDATE BALL");
					break;
				}
				
				case FETCH_BALL: {
					System.out.println("Entering FETCH BALL");
					curMove = "O";
					commandTransmitter.robotCollectBall();
					incrementBallsFetched();
					state = State.GO_TO_STARTING_POINT;
					nextNextState = State.FIND_BALL;
					System.out.println("Leaving FETCH BALL");
					break;
				}
				
				case GO_TO_STARTING_POINT: {
					System.out.println("Entering GO TO STARTING POINT");
					if(reverseDistance != 0.0) {
						System.out.println("If reverseDistance not null");
						curMove = "K:" + (int) reverseDistance;
						commandTransmitter.robotTravel(reverseDistance);
						reverseDistance = 0.0;
						state = State.WAIT_FOR_MOVE;
						nextState = State.GO_TO_STARTING_POINT;
					} else if(reverseAngle != 0.0) {
						System.out.println("If reverseAngle not null");
						reverseAngle %= 360;
						curMove = "D:" + (int) reverseAngle;
						commandTransmitter.robotTurn(reverseAngle);
						reverseAngle = 0.0;
						state = State.WAIT_FOR_MOVE;
						nextState = State.GO_TO_STARTING_POINT;
					} else {
						System.out.println("If reverseAngle+Distance null");
						if(ballCollectedCount >= 10) {
							System.out.println("If collected 10+ balls");
							state = State.COMPLETED;
						} else {
							System.out.println("If collected less than 10 balls");
							state = nextNextState;						
						}
					}
					System.out.println("Leaving GO TO STARTING POINT");
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
					state = State.COMPLETED;
					break;
				}
					
				case COMPLETED: {
					notDone = false;
					break;	
				}
			}
		}
	}
	
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = new BlackboardSample(bbSample);
	}
	
	private void incrementBallsFetched() {
		serverGUI.setBallsCollected(++ballCollectedCount + "");
	}
	
	private Point getNearestBall(LidarScan scan) {	
		Mat map = Vision.scanToLineMap(scan);
		Mat obstacles = new Mat(map.size(), map.type());
		
		//Remove shit obstacles
		Vision.findWallsAndRemove(map, obstacles);
		obstacles.release();
		
		//BAlls plz
		Mat ballMat = Vision.findAllBallsLidar(map);
		
		// roi
		Rect roi = new Rect(1500, 1500, 1000, 1000);
		map = new Mat(map, roi);
		
		// Circles plz
		Vision.drawCirclesOnMap(map, ballMat);
		List<Point> ballList = Vision.getCircleLocsFromMat(ballMat);
		ballMat.release();
		
		//Gui
		serverGUI.setCameraFrame(Vision.matToImageBuffer(map));
		map.release();
		
		Point nearestBall = null;
		double nearestDist = Double.MAX_VALUE;
		
		for(Point curBall : ballList) {
			double calcDist = ORIGIN_WHEEL.distance(curBall);
			if(calcDist < nearestDist) {
				nearestBall = curBall;
				nearestDist = calcDist;
			}
		}
		if(nearestBall != null)
			serverGUI.setBallLocation(nearestBall.x + ":" + nearestBall.y);
		
		return nearestBall;
	}
	
	private Point getNearestBallFront(LidarScan scan) {	
		LidarScan directionalScan = getFrontScans(scan);
		return getNearestBall(directionalScan);
	}
	
	public static LidarScan getFrontScans(LidarScan scan) {
		double lower = 165, upper = 195;
		LidarScan directionalScan = new LidarScan();
		for (LidarSample sample : scan.getSamples()) {
			if (sample.angle > lower && sample.angle < upper) {
				directionalScan.addSample(sample);
			}
		}
		return directionalScan;
	}
}
