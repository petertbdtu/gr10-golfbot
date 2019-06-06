package Knowledgesource;

import lejos.robotics.navigation.MovePilot;

public class KSNavigation extends KnowledgeSource<Boolean>{

	MovePilot pilot;
	
	public KSNavigation(MovePilot pilot) {
		this.pilot = pilot;
	}
	
	public void forward(double distance) {
		pilot.travel(distance, true);
	}
	
	public void turn(double angle) {
		pilot.rotate(angle, true);
	}
	
	public void stopMoving() {
		pilot.stop();
	}
	
	@Override
	protected Boolean getKnowledge() {
		return new Boolean(pilot.isMoving());
	}

	@Override
	protected byte[] getKnowledgeAsBytes() {
		byte val = pilot.isMoving() ? (byte) 1 : (byte) 0;
		return new byte[] { val };
	}
	
}
