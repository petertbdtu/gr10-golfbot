package golfbot.robot.knowledgesources;

import golfbot.robot.RobotSingle;
import golfbot.samples.NavigationSample;
import lejos.robotics.navigation.MovePilot;

public class KSNavigation extends KnowledgeSource<NavigationSample>{

	MovePilot pilot = RobotSingle.pilot;
	
	public void travelTo(double angle, double distance) {
		pilot.setLinearSpeed(500.00);
		pilot.rotate(angle, true);
		pilot.travel(distance, true);
	}
	
	public void stopMoving() {
		pilot.stop();
	}

	@Override
	protected NavigationSample getKnowledge() {
		return new NavigationSample(pilot.isMoving());
	}
	
}
