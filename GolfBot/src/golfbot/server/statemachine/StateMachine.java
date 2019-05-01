package golfbot.server.statemachine;

public class StateMachine extends Thread {
	
	private enum State {
			EXPLORE,
			PLAN_ROUTE,
			RUN_ROUTE,
			FETCH_BALL,
			GO_TO_GOAL,
			COMPLETED
	}
	private State state;
	private boolean trigger;
	private int ballcounter;
	private int ballsDelivered;
	
	public StateMachine() {
		state = State.EXPLORE;
		trigger = false;
		ballcounter = 0;
		ballsDelivered = 0;
	}
	
	public void FSM() {
		while(!trigger) {
			switch(state) {
				case EXPLORE:
					/* while (ballNotFound) ;
					 * state = State.PLAN_ROUTE;
					 * */
					break;
					
				case PLAN_ROUTE:
					/* planRoute();
					 * state = State.RUN_ROUTE;
					 */
					break;
				
				case RUN_ROUTE:
					/* driveToNearestPoint();
					 * state = State.FETCH_BALL;
					 */
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
					
					break;
					
				case COMPLETED:
					/* celebrate();
					 * trigger = TRUE;
					 */
					break;
				default:
					state = State.EXPLORE;
					break;
			}
		}
	}
	

}
