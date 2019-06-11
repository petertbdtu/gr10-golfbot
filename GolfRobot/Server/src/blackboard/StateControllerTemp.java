package blackboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import blackboard.BlackboardController;
import blackboard.BlackboardSample;
import communication.CommandTransmitter;
import communication.LegoReceiver;
import communication.LidarReceiver;
import mapping.LidarScan;
import objects.LidarSample;

public class StateControllerTemp extends Thread implements BlackboardListener  {
	
	public static void main(String[] args){
		StateControllerTemp st = new StateControllerTemp();
		st.start();
		try { st.join(); } 
		catch (InterruptedException e) { e.printStackTrace(); }
	}
	
	private enum State {
		DEBUG,
		COLLISION_AVOIDANCE_TURN,
		COLLISION_AVOIDANCE_REVERSE,
		COLLISION_AVOIDANCE_STOP,
		EXPLORE,
		VALIDATE_BALL,
		PLAN_ROUTE,
		RUN_ROUTE,
		FETCH_BALL,
		FIND_BALL,
		FIND_GOAL,
		GO_TO_GOAL,		
		COMPLETED,
		IS_MOVING,
		WAIT_FOR_RUN
	}
	
	private State nextState;
	private State state;
	private State lastState;
	
	
	
	private boolean notDone;
	private boolean ballFound;
	private int ballcounter;
	private int ballsDelivered;
	private BlackboardSample bbSample;
	private CommandTransmitter commandTransmitter;
	private LidarReceiver lidarReceiver;
	private LegoReceiver legoReceiver;
	private BlackboardController bController;
	private BLCollisionDetector collisionDetector;
	private BLBallDetector ballDetector;


	public StateControllerTemp() {
		// Initial state
		state = State.EXPLORE;
		notDone = true;
		ballcounter = 0;
		ballsDelivered = 0;
		
		// Initialise objects
		initialiseObjects();
	}

	@Override
	public void run() {
		State tempState = State.DEBUG;
		while(!notDone) {
			
			// DEBUG
			if(state != tempState) {
				System.out.println(state);
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
					state = State.WAIT_FOR_RUN;
					break;
					
				case FIND_BALL:
					LidarScan scan = bbSample.scan;
					
					// Ball found?
					if(ballDetector.getClosestBall(scan) != null) {
						// Go To Ball
					}
					
					state = State.EXPLORE;
					break;

				case GET_SAMPLES:
					commandTransmitter.robotTravel(360, 0);
					getLidarSamples();
					state = State.EXPLORE;
					break;
				case COLLISION_AVOIDANCE_STOP:
					commandTransmitter.robotStop();
					nextState = State.COLLISION_AVOIDANCE_REVERSE;
					state = State.IS_MOVING;
					break;
				case COLLISION_AVOIDANCE_REVERSE:
					commandTransmitter.robotTravel(0,50);
					nextState = State.COLLISION_AVOIDANCE_TURN;
					state = State.WAIT_FOR_RUN;
					break;
				case COLLISION_AVOIDANCE_TURN:
					commandTransmitter.robotTravel(90,0);
					cd.setIsDetected(false);
					nextState = State.RUN_ROUTE;
					state = State.WAIT_FOR_RUN;
					break;


				case VALIDATE_BALL:
					
					// TurnToBall()
					// Validate with camera()
					state = State.PLAN_ROUTE;
					break;

				case PLAN_ROUTE:
					/* planRoute();
					 * state = State.RUN_ROUTE;
					 *
					 */
				 	state = State.RUN_ROUTE;
					break;

				case RUN_ROUTE:
					/* driveToNearestPoint();
					 * state = State.FETCH_BALL;
					 */

					// MOVE ACCORDING TO ROUTEPLANNER
					//commandTransmitter.robotTravel(90, 0);
					commandTransmitter.robotTravel(0, -1000);

					nextState = State.RUN_ROUTE;
					state = State.WAIT_FOR_RUN;
					break;

				case WAIT_FOR_RUN:
					if(bbSample.isMoving)
						state = State.IS_MOVING;
					break;
				case FETCH_BALL:
					/* pickUpBall();
					 * ballcounter++;
					 * if (ballcounter == 5)
					 * 		state = State.GO_TO_GOAL;
					 * else
					 * 		if (routeUpdated())
					 * 			state = State.PLAN_ROUTE;
					 * 		else
					 * 			state = State.RUN_ROUTE;
					 */

				 	commandTransmitter.robotCollectBall();
					ballcounter++;
					if (ballcounter == 10)
						state = State.FIND_GOAL;
					else
						state = State.RUN_ROUTE;
					break;

				
				case FIND_GOAL:
					/* if (locateNearestGoal())
					 * 		state = State.GO_TO_GOAL;
					 * else
					 * 		travelAlongWall();
					 *
					 */

					state = State.GO_TO_GOAL;
					break;

				case GO_TO_GOAL:
					/* deliverBalls();
					 * ballsDelivered += ballcounter;
					 * ballcounter = 0;
					 * if ballsDelivered == 10
					 * 		state = State.COMPLETED
					 * else
					 * 		state = State.EXPLORE;
					 */

					 deliverBalls();

 					 state = State.COMPLETED;

					break;

				case COMPLETED:
					/* finished();
					 * trigger = TRUE;
					 */
					break;
					
				case IS_MOVING:
					if(!bbSample.isMoving)
						state = nextState;
					break;
					
				default:
					state = State.EXPLORE;
					break;
			}
		}
	}

	public void initialiseObjects() {
		// Very important boolean
		boolean YesRobotRunYesYes = true;

		// Build Lidar receiver
		System.out.println("Building Lidar Receiver...");
		lidarReceiver = new LidarReceiver();
		if(YesRobotRunYesYes && lidarReceiver.bindSocket(5000)) {
			lidarReceiver.start();
			System.out.println("Lidar Receiver succes");
		} else {
			YesRobotRunYesYes = false;
			System.out.println("Lidar Receiver failed");
		}

		// Build Lego Receiver
		System.out.println("Building Lego Receiver...");
		legoReceiver = new LegoReceiver();
		if(YesRobotRunYesYes && legoReceiver.connect(3000)) {  //connect(3000, 3001, 3002)
			legoReceiver.start();
			System.out.println("Lego Receiver succes");
		} else {
			YesRobotRunYesYes = false;
			System.out.println("Lego Receiver failed");
		}

		// Command Transmitter
		System.out.println("Building Command Transmitter...");
		commandTransmitter = new CommandTransmitter();
		if(YesRobotRunYesYes) {
			YesRobotRunYesYes = commandTransmitter.connect(3001);
			System.out.println("Command Transmitter succes");
		} else {
			YesRobotRunYesYes = false;
			System.out.println("Command Transmitter failed");
		}
		
		//Collision Detection
		System.out.println("Building Collision Detector...");
		cd = new BLCollisionDetector();
		if(YesRobotRunYesYes) {
			cd.start();
			System.out.println("Collision detection activated");
		} else {
			System.out.println("Collision detection aprehended");
		}

		// Blackboard Controller
		System.out.println("Building blackboard...");
		bController = new BlackboardController(null, legoReceiver, lidarReceiver);
		bController.registerListener(commandTransmitter);
		bController.registerListener(cd);
		bController.registerListener(this);
		if(YesRobotRunYesYes) {
			bController.start();
			System.out.println("Blackboard succes");
		} else {
			System.out.println("Blackboard not started");
		}
	}

	public void wallCollisionISR() {
		state = State.COLLISION_AVOIDANCE_STOP;
	}

	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = new BlackboardSample(bbSample);
		System.out.println(bbSample.isMoving);
	}

	public void getLidarSamples() {
		// read samples
	}

	public boolean locateBall() {
		// if (Ball located() == true) ballfound = true;
		
		return ballFound;
	}

	public void turnToBall() {

	}

	public void planRoute() {
		// route planner magic
	}

	public void driveToNearestPoint() {

	}

	public void pickUpBall() {

	}

	public void routeUpdated() {

	}

	public void robotEmergencyBrake() {

	}

	public void locateNearestGoal() {

	}

	public void deliverBalls() {
		//commandTransmitter.deliverBalls();
	}

	public void finished() {
		// MAKE A FINISHING SOUND
	}
	
	

}
