package blackboard;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;

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

	
	private final int FOLLOW_WALL_STEPSIZE = 200;
	private final int LIDAR_TO_FRONT_LENGTH = 360;
	private final int LIDAR_TO_RIGHT_LENGTH = 230;
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
	private LinkedList<PolarPoint> reverseQueue;

	
	
	public BLStateController(ServerGUI gui, CommandTransmitter commandTransmitter, State state) {
		this.serverGUI = gui;
		this.commandTransmitter = commandTransmitter;
		this.state = state;
		reverseQueue = new LinkedList<PolarPoint>();
		notDone = true;
	}	
	
	@Override
	public void run() {
		String curMove = "";
		LidarScan wallScanOld = new LidarScan();
		LidarScan ballScanOld = new LidarScan();
		double lengthToCorrect = 0;
		double lengthToTravel = 0;
		double angleToCorrectIn = 0;
		double angleToCorrectBack = 0;
		int ballValidationCount = 0;
		int ballFindingCount = 0;
		
		incrementBallsFetched();
		
		while(notDone) {
			
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
					if(bbSample != null && bbSample.isMoving)
						state = State.IS_MOVING;
					break;
				}
				
				/*
				 * State called with every move, to ensure command was finished by robot
				 */
				case IS_MOVING: {
					if(bbSample != null && !bbSample.isMoving)
						state = nextState;
					break;
				}
				
				/*
				 * State where the robot follows the right wall
				 */
				case FOLLOW_WALL: {
					int distForwardMax = 0;
					// Scan found
					if (bbSample != null && bbSample.scan != null) {
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						// New Scan
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) {
							
							for (LidarSample sample : wallScanNew.getSamples()) {
								// Infront of Robot & max distForward
								if (sample.angle > 175.0 && sample.angle < 185.0 && sample.distance > distForwardMax) {
									distForwardMax = (int) sample.distance;
								}
							}
							wallScanOld = wallScanNew;
						}
					}
					if(distForwardMax != 0) {
						if(distForwardMax <= LIDAR_TO_FRONT_LENGTH) { // TURN
							commandTransmitter.robotTurn(90.0);
							curMove = "D:90.0";
							nextState = State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						} else if(distForwardMax <= LIDAR_TO_FRONT_LENGTH + FOLLOW_WALL_STEPSIZE) { // SMALL STEP
							int dist = distForwardMax - LIDAR_TO_FRONT_LENGTH;
							commandTransmitter.robotTravel(dist);
							curMove = "K:" + dist;
							nextState = State.FIND_BALL;
							state = State.WAIT_FOR_MOVE;
						} else { // FULL STEP
							int dist = FOLLOW_WALL_STEPSIZE;
							commandTransmitter.robotTravel(dist);
							curMove = "K:" + dist;
							nextState = State.WALL_CORRECTION;
							state = State.WAIT_FOR_MOVE;
						}
					}
					break;
				}
				
				/*
				 * State where the angle driving is corrected corresponding to the right wall
				 */
				case WALL_CORRECTION: {
					int distRightMaxEnd = 0;
					if (bbSample != null && bbSample.scan != null) { // Scan present in BBSample
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) { // Scan in BBSample is new
							for (LidarSample s : wallScanNew.getSamples()) {
								if (s.angle > 88.0 && s.angle < 92.0 && s.distance > distRightMaxEnd) { // Points found to the right
									distRightMaxEnd = (int) s.distance;
								}
							}
							wallScanOld = wallScanNew;
							
							if(distRightMaxEnd != 0 && distRightMaxEnd < 300) {					
								lengthToCorrect = distRightMaxEnd - LIDAR_TO_RIGHT_LENGTH;
								angleToCorrectIn = (Math.toDegrees(Math.asin(lengthToCorrect/FOLLOW_WALL_STEPSIZE))) * -1;
								if((angleToCorrectIn < -5 || angleToCorrectIn > 5) && distRightMaxEnd < 300) {
									state = State.WALL_CORRECTION_TURNSTRAIGHT;
								} else {
									state = State.FIND_BALL; 
								}
							} else {
								state = State.FIND_BALL;
							}
						}
					}
					break;
				}
				
				case WALL_CORRECTION_TURNSTRAIGHT: {
					curMove = "D:" + angleToCorrectIn;
					commandTransmitter.robotTurn(angleToCorrectIn);
					nextState = State.WALL_CORRECTION_TURNIN;
					state = State.WAIT_FOR_MOVE;
					break;
				}
				
				case WALL_CORRECTION_TURNIN: {
					double distForwardMax = 0;
					// Scan found
					if (bbSample != null && bbSample.scan != null) {
						LidarScan wallScanNew = new LidarScan(bbSample.scan);
						// New Scan
						if(wallScanNew.scanSize() != wallScanOld.scanSize()) {
							
							for (LidarSample sample : wallScanNew.getSamples()) {
								// Infront of Robot & max distForward
								if (sample.angle > 175.0 && sample.angle < 185.0 && sample.distance > distForwardMax) {
									distForwardMax = sample.distance;
								}
							}
							wallScanOld = wallScanNew;						
						}
					}
					if(distForwardMax != 0) {
						double distForward = distForwardMax > FOLLOW_WALL_STEPSIZE ? FOLLOW_WALL_STEPSIZE / 2.0 : distForwardMax / 2.0;
						angleToCorrectBack = (Math.toDegrees(Math.atan(lengthToCorrect / distForward))) * -1;
						lengthToTravel = Math.sqrt(Math.pow(lengthToCorrect, 2) + Math.pow(distForward, 2));
						curMove = "D:" + angleToCorrectBack;
						commandTransmitter.robotTurn(angleToCorrectBack);
						nextState = State.WALL_CORRECTION_TRAVEL;
						state = State.WAIT_FOR_MOVE;
					}
					break;
				}
				
				case WALL_CORRECTION_TRAVEL: {
					curMove = "K:" + lengthToTravel;
					commandTransmitter.robotTravel(lengthToTravel);
					nextState = State.WALL_CORRECTION_TURNBACK;
					state = State.WAIT_FOR_MOVE;
					break;
				}
				
				case WALL_CORRECTION_TURNBACK: {
					curMove = "D:" + angleToCorrectBack * -1;
					commandTransmitter.robotTurn(angleToCorrectBack * -1);
					nextState = State.FIND_BALL;
					state = State.WAIT_FOR_MOVE;
					break;
				}

	
				case FIND_BALL:{
					if(ballFindingCount >= BALL_FINDING_MAX_COUNT) {
						nextNextState = State.FOLLOW_WALL;
						state = State.GO_TO_STARTING_POINT;
						ballFindingCount = 0;
					} else if(bbSample != null && bbSample.scan != null) {
						LidarScan ballScanNew = new LidarScan(bbSample.scan);
						// New Scan
						if(ballScanNew.scanSize() != ballScanOld.scanSize()) {
							Point nearestBall = getNearestBall(ballScanNew);
							if(nearestBall != null){
								ballFindingCount++;
								double ballAngle = ORIGIN_WHEEL.angleTo(nearestBall);
								reverseQueue.addFirst(new PolarPoint(-ballAngle, 0));
								commandTransmitter.robotTurn(ballAngle);
								curMove = "D:" + (int)ballAngle;
								nextState = State.VALIDATE_BALL;
								state = State.WAIT_FOR_MOVE;
							} else {
								state = State.FOLLOW_WALL;
							}
							ballScanOld = ballScanNew;						
						}			
						
					}
					break;
				}
				
				case VALIDATE_BALL: {
					if(ballValidationCount >= BALL_VALIDATION_MAX_COUNT) {
						ballValidationCount = 0;
						state = State.GO_TO_STARTING_POINT;
						nextNextState = State.FIND_BALL;
					} else if(bbSample != null && bbSample.scan != null) {
						LidarScan ballScanNew = new LidarScan(bbSample.scan);
						if(ballScanNew.scanSize() != ballScanOld.scanSize()) { // NEW SCAN
							Point nearestBall = getNearestBallFront(ballScanNew);
							if(nearestBall != null) {
								double ballAngle = ORIGIN_WHEEL.angleTo(nearestBall);
								if(ballAngle > -4 && ballAngle < 4) { //Distance command
									ballValidationCount = 0;
									double ballDistance = ORIGIN_TUBE.distance(nearestBall.x, nearestBall.y);				
									reverseQueue.addFirst(new PolarPoint(0,-ballDistance));
									commandTransmitter.robotTravel(ballDistance);
									curMove = "K:" + (int)ballDistance;
									nextState = State.FETCH_BALL;
									state = State.WAIT_FOR_MOVE;
								} else { //Heading command
									ballValidationCount++;
									reverseQueue.addFirst(new PolarPoint(-ballAngle, 0));
									commandTransmitter.robotTurn(ballAngle);
									curMove = "D:" + (int)ballAngle;
									nextState = State.VALIDATE_BALL;
									state = State.WAIT_FOR_MOVE;
								}
							} else {
								state = State.FIND_BALL;
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
					if(reverseQueue.isEmpty()) {
						if(ballCollectedCount >= 10) {
							state = State.COMPLETED;
						} else {
							state = nextNextState;						}
					} else {
						PolarPoint command = reverseQueue.pop();
						
						if(command.distance != 0) {
							curMove = "K:" + (int)command.distance;
							commandTransmitter.robotTravel(command.distance);
						} else {
							curMove = "D:" + (int)command.angle;
							commandTransmitter.robotTurn(command.angle);
						}
						nextState = State.GO_TO_STARTING_POINT;
						state = State.WAIT_FOR_MOVE;	
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
					curMove = "A";
					commandTransmitter.robotDeliverBalls();
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
		Mat map = Vision.scanToPointMap(scan);
		Mat obstacles = new Mat(map.size(), map.type());
		//Remove shit obstacles
		Vision.findWallsAndRemove(map, obstacles);
		obstacles.release();
		Mat ballMat = Vision.findAllBallsLidar(map);
		
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
