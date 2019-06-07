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

public class BLController implements BlackboardListener {
	private enum State {
		GET_SAMPLES,
		COLLISION_AVOIDANCE,
		EXPLORE,
		VALIDATE_BALL,
		PLAN_ROUTE,
		RUN_ROUTE,
		FETCH_BALL,
		FIND_GOAL,
		GO_TO_GOAL,
		
		COMPLETED
	}
	private State state;
	private State lastState;
	private boolean trigger;
	private boolean ballFound;
	private int ballcounter;
	private int ballsDelivered;
	private BlackboardSample bbSample;
	private CommandTransmitter commandTransmitter;
	private LidarReceiver lidarReceiver;
	private LegoReceiver legoReceiver;
	private BlackboardController bController;


	public BLController() {
		state = State.EXPLORE;
		trigger = false;
		ballFound = false;
		ballcounter = 0;
		ballsDelivered = 0;
	}

	public void main(){
		startup();
		FSM();
	}

	public void FSM() {
		while(!trigger) {
			if (collisionDetected == true) { // collisionDetected skal sættes af en anden classe
				lastState = state;
				state = State.COLLISION_AVOIDANCE;
			}
			
			switch(state) {
				case GET_SAMPLES:
					
					commandTransmitter.robotTravel(360, 0);
					getLidarSamples();
					state = State.EXPLORE;
					break;
				
				case COLLISION_AVOIDANCE:
					// Go back, compare right side to left side, turn and go where the distance is largest
					commandTransmitter.robotTravel(0,500);
					commandTransmitter.robotTravel(90,0);
					
					collisionDetected = false;
					state = lastState;
					break;


				case EXPLORE:
					
					// Follow left wall, robot drives clockwise

					if (locateBall() == false) {
						state = State.EXPLORE;
					} else {
						state = State.VALIDATE_BALL;
					}
					break;

				case VALIDATE_BALL:
					/* turnToBall();
					 * if( stillABall() ){
					 * 	state = state.PLAN_ROUTE;
				 	 * } else state = state.EXPLORE;
					 * */
					
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
					commandTransmitter.robotTravel(90, 0);
					commandTransmitter.robotTravel(0, 1000);

					state = State.FETCH_BALL;
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

				default:
					state = State.EXPLORE;
					break;
			}
		}
	}

	public void startup() {
		// Very important boolean
		boolean YesRobotRunYesYes = true;

		// Build Lidar receiver
		System.out.println("Building Lidar Receiver...");
		LidarReceiver lidarReceiver = new LidarReceiver();
		if(YesRobotRunYesYes && lidarReceiver.bindSocket(5000)) {
			lidarReceiver.start();
			System.out.println("Lidar Receiver succes");
		} else {
			YesRobotRunYesYes = false;
			System.out.println("Lidar Receiver failed");
		}

		// Build Lego Receiver
		System.out.println("Building Lego Receiver...");
		LegoReceiver legoReceiver = new LegoReceiver();
		if(YesRobotRunYesYes && legoReceiver.connect(3000)) {  //connect(3000, 3001, 3002)
			legoReceiver.start();
			System.out.println("Lego Receiver succes");
		} else {
			YesRobotRunYesYes = false;
			System.out.println("Lego Receiver failed");
		}

		// Command Transmitter
		System.out.println("Building Command Transmitter...");
		CommandTransmitter commandTransmitter = new CommandTransmitter();
		if(YesRobotRunYesYes) {
			YesRobotRunYesYes = commandTransmitter.connect(3003);
			System.out.println("Command Transmitter succes");
		} else {
			YesRobotRunYesYes = false;
			System.out.println("Command Transmitter failed");
		}

		// Blackboard Controller
		System.out.println("Building blackboard...");
		BlackboardController bController = new BlackboardController(null, legoReceiver, lidarReceiver);
		bController.registerListener(commandTransmitter);
		if(YesRobotRunYesYes) {
			bController.start();
			System.out.println("Blackboard succes");
		} else {
			System.out.println("Blackboard not started");
		}
	}

	public void wallCollisionISR() {
		state = State.COLLISION_AVOIDANCE;
	}

	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = bbSample;
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
