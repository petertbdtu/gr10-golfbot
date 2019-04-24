package golfbot.robot.knowledgesources;

import lejos.robotics.navigation.MovePilot;

public class KSNavigation extends KnowledgeSource<Boolean>{

	MovePilot pilot;
	
	public KSNavigation(MovePilot pilot) {
		this.pilot = pilot;
	}
	
	public void travelTo(double angle, double distance) {
		pilot.setLinearSpeed(500.00);
		pilot.rotate(angle, true);
		pilot.travel(distance, true);
	}
	
	public void stopMoving() {
		pilot.stop();
	}

	@Override
	protected Boolean getKnowledge() {
		return new Boolean(pilot.isMoving());
	}
	
}
