package blackboard;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import blackboard.BlackboardSample;
import communication.CommandTransmitter;
import gui.ServerGUI;
import mapping.LidarScan;
import mapping.Vision;
import objects.LidarSample;
import objects.Point;

public class BLStateController extends Thread implements BlackboardListener  {
	
	public volatile int ballCollectedCount = -1;
	public volatile boolean pauseStateMachine = false;
	public volatile boolean stopStateMachine = false;
	private volatile long startTime = 0;
	private volatile long goalTime = 0;
	private volatile long endTime = 0;
	private volatile boolean findGoal = false;
	private volatile BlackboardSample bbSample;
	
	private final int MAX_BALL_DISTANCE = 300;
	private final int LIDAR_TO_FRONT_LENGTH = 360;
	private final int LIDAR_TO_RIGHT_LENGTH = 260;
	private final int GOAL_DISTANCE_CORRECTION = 100;
	private final int BALL_DISTANCE_CORRECTION = -5;
	private final int BALL_VALIDATION_MAX_COUNT = 3;
	private final int BALL_FINDING_MAX_COUNT = 3;
	private final Point ORIGIN_WHEEL = new Point(107,0);
	private final Point ORIGIN_TUBE = new Point(155,0);
	
	public static enum State {
		DEBUG,
		PAUSE,
		
		IS_MOVING,
		WAIT_FOR_MOVE,
				
		FOLLOW_WALL,
		WALL_CORRECTION,
		WALL_CORRECTION_TURNIN,
		WALL_CORRECTION_TRAVEL,
		WALL_CORRECTION_TURNBACK,
		
		FIND_BALL,
		VALIDATE_BALL,
		GO_TO_BALL,
		TURN_TO_BALL,
		GO_TO_STARTING_POINT,
		FETCH_BALL,

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
	private double followWallStepSize = 200;
	
	public BLStateController(ServerGUI gui, CommandTransmitter commandTransmitter, State state) {
		this.serverGUI = gui;
		this.commandTransmitter = commandTransmitter;
		this.state = state;
		if(state == State.FIND_GOAL) {
			followWallStepSize = 100;
			findGoal = true;
		}
			
		startTime = System.currentTimeMillis();
		endTime = startTime + 8 * 60 * 1000;
		goalTime = startTime + 6 * 60 * 1000;
		
		this.serverGUI.setGoalFinding(findGoal + "");
		this.serverGUI.setTimer(startTime);
		notDone = true;
	}	
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void run() {
		String curMove = "";
		LidarScan oldScan = new LidarScan();
		boolean followWallTurnState = false;
		int ballValidationCount = 0;
		int ballFindingCount = 0;
		State tempState = State.DEBUG;
		incrementBallsFetched();
		int distToGoalX = 0;
		int distToGoalY = 0;
		double angleToGoal = 0;
		Point goal = null;
		
		// Correction values
		double correctionTravel = 1, correctionTurnIn = 1, correctionTurnStraight = 1, correctionTurnBack = 1;
		
		
		while(notDone) {
			
			if(System.currentTimeMillis() > goalTime && !findGoal) {
				findGoal = true;
				serverGUI.setGoalFinding(findGoal + "");
			}
			
			if(System.currentTimeMillis() > endTime - 6000) {
				state = State.DELIVER_BALLS;
			}
			// Pause
			if(state != tempState && state != State.IS_MOVING && state != State.WAIT_FOR_MOVE) {
				tempState = state;
			}	
			
			// Pause
			if(pauseStateMachine && state != State.IS_MOVING && state != State.PAUSE) {
				nextState = state;
				state = State.PAUSE;
			}			
			
			// Update GUI
			if(state != State.IS_MOVING && state != State.WAIT_FOR_MOVE)
				serverGUI.setState(state.toString());
			serverGUI.setLastMove(curMove);

			// State switcher
			switch(state) {		
			
				case PAUSE: {
					if(!pauseStateMachine) { state = nextState; } 
					break;
				}
				

				/*
				 * State called with every move, to ensure command was received by robot
				 */
				case WAIT_FOR_MOVE: {
					if(bbSample != null && bbSample.isMoving) { state = State.IS_MOVING; }
					break;
				}
				
				/*
				 * State called with every move, to ensure command was finished by robot
				 */
				case IS_MOVING: {
					if(bbSample != null && !bbSample.isMoving) { state = nextState; }
					break;
				}
				
				/*
				 * State where the robot follows the right wall
				 */
				case FOLLOW_WALL: {
					int distForwardMax = 0;
					
					if (bbSample != null && bbSample.scan != null) { // Scan found
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != oldScan.scanSize()) { // New Scan
							for (LidarSample sample : wallScanNew.getSamples()) {
								if (sample.angle > 175.0 && sample.angle < 185.0 && sample.distance > distForwardMax) { // Infront of Robot & max distForward
									distForwardMax = (int) sample.distance;
								}
							}
							oldScan = wallScanNew;
						}
					}
					if(distForwardMax != 0) {
						if(followWallTurnState || distForwardMax <= LIDAR_TO_FRONT_LENGTH) { // TURN
							followWallTurnState = !followWallTurnState;
							commandTransmitter.robotTurn(45.0);
							curMove = "D:90.0";
							nextState = findGoal ? State.FIND_GOAL : State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						} else if(distForwardMax <= LIDAR_TO_FRONT_LENGTH + followWallStepSize) { // SMALL STEP
							int dist = distForwardMax - LIDAR_TO_FRONT_LENGTH;
							commandTransmitter.robotTravel(dist);
							curMove = "K:" + dist;
							nextState = findGoal ? State.FIND_GOAL : State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						} else { // FULL STEP
							curMove = "K:" + (int) followWallStepSize;
							commandTransmitter.robotTravel(followWallStepSize);
							nextState = findGoal ? State.FIND_GOAL : State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						}
					}
					break;
				}
				
				/*
				 * State where the angle driving is corrected corresponding to the right wall
				 */
				case WALL_CORRECTION: {
					int distRight = Integer.MAX_VALUE;
					LidarSample sampleLeft = new LidarSample(0, Integer.MAX_VALUE);
					if (bbSample != null && bbSample.scan != null) { // Scan present in BBSample
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != oldScan.scanSize()) { // Scan in BBSample is new
							for (LidarSample s : wallScanNew.getSamples()) {
								if (s.angle > 88.0 && s.angle < 92.0 && s.distance < distRight) { // Points found to the right
									distRight = (int) s.distance;
								}
								if(s.angle > 104 && s.angle < 108 && s.distance < sampleLeft.distance) {
									sampleLeft = s;
								}
							}
							oldScan = wallScanNew;
							
							if((distRight != Integer.MAX_VALUE && distRight < 1000) && (sampleLeft.distance != Integer.MAX_VALUE && sampleLeft.distance < 1000)) {
								Point pRight = new LidarSample(90, distRight).getRectangularCoordinates();
								Point pLeft = sampleLeft.getRectangularCoordinates();
								double alpha = ( (double) (pRight.y - pLeft.y) / (double) (pRight.x - pLeft.x) );
								correctionTurnStraight = Math.toDegrees(Math.atan(alpha));
								if(correctionTurnStraight < -3 || correctionTurnStraight > 3) {
									curMove = "D:" + (int)correctionTurnStraight;
									commandTransmitter.robotTurn(correctionTurnStraight);
									nextState = State.WALL_CORRECTION_TURNIN;
									state = State.WAIT_FOR_MOVE;
								} else {
									state = State.WALL_CORRECTION_TURNIN;
								}
							} else {
								state = State.FOLLOW_WALL;
							}
						}
					}
					break;
				}
				
				case WALL_CORRECTION_TURNIN: {
					int distRight = Integer.MAX_VALUE;
					if (bbSample != null && bbSample.scan != null) { // Scan present in BBSample
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != oldScan.scanSize()) { // Scan in BBSample is new
							for (LidarSample s : wallScanNew.getSamples()) {
								if (s.angle > 88.0 && s.angle < 92.0 && s.distance < distRight) { // Points found to the right
									distRight = (int) s.distance;
								}
							}
							
							oldScan = wallScanNew;
							
							if(distRight != Integer.MAX_VALUE && distRight < 1000) {	
								// Distance Error
								double correctionError = distRight - LIDAR_TO_RIGHT_LENGTH;
								
								// Wanted location
								Point correctionPoint = new Point( (int) (ORIGIN_WHEEL.x-(followWallStepSize/3)) , (int) (correctionError * -1) );
								System.out.println("CORRECTION POINT: " + correctionPoint.x + ":" + correctionPoint.y);
								
								// Turn In
								correctionTurnIn = ORIGIN_WHEEL.angleTo(correctionPoint);
								System.out.println("TURN IN: " + correctionTurnIn);
								correctionTurnIn = correctionTurnIn > 0 ? correctionTurnIn-180 : 180+correctionTurnIn;
								System.out.println("TURN IN: " + correctionTurnIn);
								
								// Travel distance
								correctionTravel = ORIGIN_WHEEL.distance(correctionPoint) * -1;
								System.out.println("TRAVEL: " + correctionTravel);
								
								// Turn Back
								correctionTurnBack = Math.toDegrees(Math.acos((ORIGIN_WHEEL.x-correctionPoint.x)/correctionTravel));
								System.out.println("TURN BACK: " + correctionTurnBack);
								correctionTurnBack = correctionError > 0 ? (180-correctionTurnBack) * -1 : 180-correctionTurnBack;
								System.out.println("TURN BACK: " + correctionTurnBack);
								
								// Test straightness
								if(correctionTurnIn < -10 || correctionTurnIn > 10) {
									curMove = "D:" + (int)correctionTurnIn;
									commandTransmitter.robotTurn(correctionTurnIn);
									nextState = State.WALL_CORRECTION_TRAVEL;
									state = State.WAIT_FOR_MOVE;
								} else {
									state = State.FOLLOW_WALL;
								}
							} else {
								state = State.FOLLOW_WALL;
							}
						}
					}
					
					

					break;
				}
				
				case WALL_CORRECTION_TRAVEL: {
					curMove = "K:" + (int)correctionTravel;
					commandTransmitter.robotTravel(correctionTravel);
					nextState = State.WALL_CORRECTION_TURNBACK;
					state = State.WAIT_FOR_MOVE;
					break;
				}
				
				case WALL_CORRECTION_TURNBACK: {
					curMove = "D:" + (int)correctionTurnBack;
					commandTransmitter.robotTurn(correctionTurnBack);
					nextState = State.FOLLOW_WALL;
					state = State.WAIT_FOR_MOVE;
					break;
				}

				case FIND_BALL:{
					if(ballFindingCount >= BALL_FINDING_MAX_COUNT) {
						if(followWallTurnState) { nextNextState = State.FOLLOW_WALL; }
						else { nextNextState = State.WALL_CORRECTION; }
						state = State.GO_TO_STARTING_POINT;
						ballFindingCount = 0;
					} else if(bbSample != null && bbSample.scan != null) {
						LidarScan ballScanNew = new LidarScan(bbSample.scan);
						if(ballScanNew.scanSize() != oldScan.scanSize()) { // New Scan
							Point nearestBall = getNearestBall(ballScanNew);
							ballFindingCount++;
							if(nearestBall != null){
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
							oldScan = ballScanNew;						
						}			
					}
					break;
				}
				
				case VALIDATE_BALL: {
					if(ballValidationCount >= BALL_VALIDATION_MAX_COUNT) {
						ballValidationCount = 0;
						state = State.GO_TO_STARTING_POINT;
						nextNextState = State.FIND_BALL;
					} else if(bbSample != null && bbSample.scan != null) { // FOUND SCAN
						LidarScan ballScanNew = new LidarScan(bbSample.scan);
						if(ballScanNew.scanSize() != oldScan.scanSize()) { // NEW SCAN
							Point nearestBall = getNearestBallFront(ballScanNew);
							if(nearestBall != null) {
								double ballAngle = ORIGIN_WHEEL.angleTo(nearestBall);
								serverGUI.setBallHeading((int) ballAngle + "");
								if(ballAngle > -3 && ballAngle < 3) { //Distance command
									ballValidationCount = 0;
									double ballDistance = ORIGIN_TUBE.distance(nearestBall.x, nearestBall.y) + BALL_DISTANCE_CORRECTION;	
									reverseDistance += -ballDistance;
									commandTransmitter.robotTravel(ballDistance);
									curMove = "K:" + (int)ballDistance;
									nextState = State.FETCH_BALL;
									state = State.WAIT_FOR_MOVE;
								} else { //Heading command
									ballValidationCount++;
									reverseAngle += -ballAngle;
									commandTransmitter.robotTurn(ballAngle);
									curMove = "D:" + (int)ballAngle;
									nextState = State.VALIDATE_BALL;
									state = State.WAIT_FOR_MOVE;
								}
							} else {
								state = State.FIND_BALL;
								ballValidationCount = 0;
							}
						}
					}
					break;
				}
				
				case FETCH_BALL: {
					curMove = "O";
					commandTransmitter.robotCollectBall();
					incrementBallsFetched();
					state = State.GO_TO_STARTING_POINT;
					nextNextState = State.FIND_BALL;
					break;
				}
				
				case GO_TO_STARTING_POINT: {
					if(reverseDistance != 0.0) {
						curMove = "K:" + (int) reverseDistance;
						commandTransmitter.robotTravel(reverseDistance);
						reverseDistance = 0.0;
						state = State.WAIT_FOR_MOVE;
						nextState = State.GO_TO_STARTING_POINT;
					} else if(reverseAngle != 0.0) {
						reverseAngle %= 360;
						curMove = "D:" + (int) reverseAngle;
						commandTransmitter.robotTurn(reverseAngle);
						reverseAngle = 0.0;
						state = State.WAIT_FOR_MOVE;
						nextState = State.GO_TO_STARTING_POINT;
					} else {
						if(ballCollectedCount >= 10) {
							findGoal = true;
							serverGUI.setGoalFinding(findGoal + "");
							followWallStepSize = 100;
							if(followWallTurnState) state = State.FOLLOW_WALL;
							else state = State.WALL_CORRECTION;
						} else {
							state = nextNextState;						
						}
					}
					break;
				}
				
				case FIND_GOAL: {
					if (bbSample != null && bbSample.scan != null) {
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != oldScan.scanSize()) { // New Scan	
							goal = findGoal(wallScanNew);
							if(goal != null) {	
								distToGoalX = goal.x - ORIGIN_WHEEL.x;
								distToGoalY = ORIGIN_WHEEL.y - goal.y;
								Point angleToTurnGoal = new Point(distToGoalX, distToGoalY);
								angleToGoal = angleToTurnGoal.angleTo(goal);
								state = State.GO_TO_GOAL;
							} else {
								if(followWallTurnState) state = State.FOLLOW_WALL;
								else state = State.WALL_CORRECTION;
							}
							oldScan = wallScanNew;						
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
		Mat redMat = mapToShow.clone().setTo(new Scalar(0,0,255));
		
		// obstacles
		Mat obstacles = Vision.findWalls(map);
		blackMat.copyTo(map, obstacles);
		redMat.copyTo(mapToShow, obstacles);
		
		// Balls
		Mat ballMat = Vision.findAllBallsLidar(map);
		List<Point> ballList = Vision.getCircleLocsFromMat(ballMat);
		Vision.drawCirclesOnMap(mapToShow, ballMat);
		
		//GUI
		serverGUI.setLidarAnalyzedFrame(Vision.matToImageBuffer(mapToShow));
		
		//Clean
		map.release();
		mapToShow.release();
		blackMat.release();
		redMat.release();
		obstacles.release();
		ballMat.release();
		
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
		serverGUI.setLidarAnalyzedFrame(Vision.matToImageBuffer(mapToShow));
		
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
