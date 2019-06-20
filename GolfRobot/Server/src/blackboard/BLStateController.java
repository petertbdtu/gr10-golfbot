package blackboard;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

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
	private final int LIDAR_TO_FRONT_LENGTH = 380;
	private final int LIDAR_TO_RIGHT_LENGTH = 260;
	private final int GOAL_DISTANCE_CORRECTION = 100;
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
	private int followWallStepSize = 200;
	private boolean findGoal = false;
	private double correctionAngle2;
	private double correctionDistance;
	private double correctionAngle4;
	private float correctionAngle1;
	
	public BLStateController(ServerGUI gui, CommandTransmitter commandTransmitter, State state) {
		this.serverGUI = gui;
		this.commandTransmitter = commandTransmitter;
		this.state = state;
		if(state == State.FIND_GOAL) {
			followWallStepSize = 100;
			findGoal = true;
		}
			
		notDone = true;
	}	
	
	@Override
	public void run() {
		String curMove = "";
		LidarScan wallScanOld = new LidarScan();
		LidarScan ballScanOld = new LidarScan();
		double correctionBackDist = 1;
		double correctionVinkel4 = 1;
		double correctionVinkel3 = 1;
		boolean followWallTurnState = false;
		int ballValidationCount = 0;
		int ballFindingCount = 0;
		State tempState = State.DEBUG;
		incrementBallsFetched();
		int distToGoalX = 0;
		int distToGoalY = 0;
		double angleToGoal = 0;
		Point goal = null;
		
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
							nextState = findGoal ? State.FIND_GOAL : State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						} else if(distForwardMax <= LIDAR_TO_FRONT_LENGTH + followWallStepSize) { // SMALL STEP
							System.out.println("If dist <= LIDAR FRONT LENGTH + STEPSIZE");
							int dist = distForwardMax - LIDAR_TO_FRONT_LENGTH;
							commandTransmitter.robotTravel(dist);
							curMove = "K:" + dist;
							nextState = findGoal ? State.FIND_GOAL : State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						} else { // FULL STEP
							System.out.println("If dist too large");
							int dist = followWallStepSize;
							commandTransmitter.robotTravel(dist);
							curMove = "K:" + dist;
							nextState = findGoal ? State.FIND_GOAL : State.FIND_BALL;
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
					int distRight = Integer.MAX_VALUE;
					if (bbSample != null && bbSample.scan != null) { // Scan present in BBSample
						System.out.println("If bbSample not null");
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) { // Scan in BBSample is new
							System.out.println("If sample is new");
							for (LidarSample s : wallScanNew.getSamples()) {
								if (s.angle > 88.0 && s.angle < 92.0 && s.distance < distRight) { // Points found to the right
									distRight = (int) s.distance;
								}
							}
							wallScanOld = wallScanNew;
							if(distRight != 0 && distRight < 1000) {	
								System.out.println("if distRightMax not 0 and < 1000");
								int correctionError = distRight - LIDAR_TO_RIGHT_LENGTH;
								Point correctionPoint = new Point(ORIGIN_WHEEL.x-(followWallStepSize/3), correctionError * -1);
								correctionAngle1 = ORIGIN_WHEEL.angleTo(correctionPoint);
								correctionAngle1 = correctionAngle1 > 0 ? correctionAngle1-180 : 180+correctionAngle1;
								System.out.println("Angle1: " + correctionAngle1);
								System.out.println("Angle2: " + correctionAngle2);
								correctionDistance = ORIGIN_WHEEL.distance(correctionPoint) * -1;
								System.out.println("Distance: " + correctionDistance);
								double correctionAngle3 = Math.toDegrees(Math.asin(correctionPoint.x/correctionDistance));
								System.out.println("Angle3: " + correctionAngle3);
								correctionAngle4 = correctionAngle3 * -1;//correctionAngle3 > 0 ? 90-correctionAngle3 : 90+correctionAngle3;
								System.out.println("Angle4: " + correctionAngle4);
								if(correctionAngle1 < -10 || correctionAngle1 > 10) {
									state = State.WALL_CORRECTION_TURNIN;
								} else {
									state = State.FOLLOW_WALL;
								}
							} else {
								state = State.FOLLOW_WALL;
							}
						}
					}
					System.out.println("Leaving WALL CORRECTION");
					break;
				}
				
				case WALL_CORRECTION_TURNIN: {
					System.out.println("Entering WALL CORRECTION TURNIN");
					curMove = "D:" + (int)correctionAngle1;
					commandTransmitter.robotTurn(correctionAngle1);
					nextState = State.WALL_CORRECTION_TRAVEL;
					state = State.WAIT_FOR_MOVE;
					break;
				}
				
				case WALL_CORRECTION_TRAVEL: {
					System.out.println("Entering WALL CORRECTION TRAVEL");
					curMove = "K:" + (int)correctionDistance;
					commandTransmitter.robotTravel(correctionDistance);
					nextState = State.WALL_CORRECTION_TURNBACK;
					state = State.WAIT_FOR_MOVE;
					break;
				}
				
				case WALL_CORRECTION_TURNBACK: {
					System.out.println("Entering WALL CORRECTION TURNBACK");
					curMove = "D:" + (int)correctionAngle4;
					commandTransmitter.robotTurn(correctionAngle4);
					nextState = State.COMPLETED;
					state = State.WAIT_FOR_MOVE;
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
							findGoal = true;
							followWallStepSize = 100;
							if(followWallTurnState) state = State.FOLLOW_WALL;
							else state = State.WALL_CORRECTION;
						} else {
							System.out.println("If collected less than 10 balls");
							state = nextNextState;						
						}
					}
					System.out.println("Leaving GO TO STARTING POINT");
					break;
				}
				
				case FIND_GOAL: {
					System.out.println("FIND GOAL :ssss");
					if (bbSample != null && bbSample.scan != null) {
						System.out.println("FIND GOAL: FOUND SCAN");
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) { // New Scan	
							System.out.println("FIND GOAL: FOUND NEW SCAN");
							goal = findGoal(wallScanNew);
							if(goal != null) {	
								state = State.VALIDATE_GOAL;
							} else {
								if(followWallTurnState) state = State.FOLLOW_WALL;
								else state = State.WALL_CORRECTION;
							}
							wallScanOld = wallScanNew;						
						}
					}
					break;
				}
					
				case VALIDATE_GOAL: {
					System.out.println("VILDATE DA GOAL PLZ :ssss");
					if (bbSample != null && bbSample.scan != null) {
						System.out.println("FIND GOAL: FOUND SCAN");
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) { // New Scan	
							System.out.println("FIND GOAL: FOUND NEW SCAN");
							goal = findGoal(wallScanNew);
							if(goal != null) {	
								distToGoalX = goal.x - ORIGIN_WHEEL.x;
								distToGoalY = ORIGIN_WHEEL.y - goal.y;
								Point angleToTurnGoal = new Point(distToGoalX, distToGoalY);
								angleToGoal = angleToTurnGoal.angleTo(goal);
								System.out.println("Angle to goal :" + angleToGoal);
								state = State.GO_TO_GOAL;
							} else {
								if(followWallTurnState) state = State.FOLLOW_WALL;
								else state = State.WALL_CORRECTION;
							}
							wallScanOld = wallScanNew;						
						}
					}
					break;
				}

				case GO_TO_GOAL: {
					if(distToGoalX != 0) {
						commandTransmitter.robotTravel(distToGoalX);
						distToGoalX = 0;
						state = State.WAIT_FOR_MOVE;
						nextState = State.GO_TO_GOAL;
					} else if(angleToGoal != 0) {
						commandTransmitter.robotTurn(angleToGoal);
						angleToGoal = 0;
						state = State.WAIT_FOR_MOVE;
						nextState = State.GO_TO_GOAL;
					} else {
						commandTransmitter.robotTravel(distToGoalY - GOAL_DISTANCE_CORRECTION);
						distToGoalY = 0;
						state = State.WAIT_FOR_MOVE;
						nextState = State.DELIVER_BALLS;
					}
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
		Mat mapToShow = new Mat();
		Imgproc.cvtColor(map, mapToShow, Imgproc.COLOR_GRAY2BGR); // ALLOW COLORS

		// Masks
		Mat blackMat = Mat.zeros(map.size(), map.type());
		Mat redMat = map.clone().setTo(new Scalar(0,0,255));
		
		// obstacles
		Mat obstacles = Vision.findWalls(map);
		blackMat.copyTo(map, obstacles);
		redMat.copyTo(mapToShow, obstacles);
		
		// Balls
		Mat ballMat = Vision.findAllBallsLidar(map);
		List<Point> ballList = Vision.getCircleLocsFromMat(ballMat);
		Vision.drawCirclesOnMap(mapToShow, ballMat);
		
		//GUI
		serverGUI.setCameraFrame(Vision.matToImageBuffer(mapToShow));
		
		//Clean
		map.release();
		mapToShow.release();
		blackMat.release();
		redMat.release();
		obstacles.release();
		
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
	public Point findGoal(LidarScan scan) {
		// Remove Angles
		LidarScan fewerAngles = new LidarScan();
		for(LidarSample s : bbSample.scan.getSamples()) {
			if(s.angle > 45.0 && s.angle < 115.0 && s.distance < 1000)
				fewerAngles.addSample(s);
		}
		
		// Make map
		Mat map = Vision.scanToLineMap(fewerAngles);
		Mat mapToShow = new Mat();
		Imgproc.cvtColor(map, mapToShow, Imgproc.COLOR_GRAY2BGR); // ALLOW COLORS
		
		// Masks
		Mat blackMat = Mat.zeros(map.size(), map.type());
		Mat redMat = mapToShow.clone().setTo(new Scalar(0,0,255));
		
		// obstacles
		Mat obstacles = Vision.findWalls(map);
		redMat.copyTo(mapToShow, obstacles);
			
		// Goal
		Point goal = Vision.findGoal(map, mapToShow);
		if (goal != null)  Vision.drawGoalPoint(mapToShow, goal);
		
		// Send to GUI
		serverGUI.setCameraFrame(Vision.matToImageBuffer(mapToShow));
		
		// Memory fix
		map.release();
		mapToShow.release();
		blackMat.release();
		redMat.release();
		
		return goal;
	}
	
	public boolean detectGoal(LidarScan scan) {
		double minAngle = 88;
		double maxAngle = 92;
		double minDist = 1000;
		
		for (LidarSample sample : scan.getSamples()) {
			if (sample.angle > minAngle && sample.angle < maxAngle && sample.distance > minDist) {
				return true;				
			}
		}
		
		return false;
	}
}
