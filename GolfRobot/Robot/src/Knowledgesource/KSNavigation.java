package Knowledgesource;

import lejos.robotics.navigation.MovePilot;

public class KSNavigation extends KnowledgeSource {

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
	
	public void slowDown() {
		pilot.setLinearSpeed(pilot.getLinearSpeed()/4);
	}

	@Override
	protected byte[] getKnowledgeAsBytes() {
		byte val = pilot.isMoving() ? (byte) 1 : (byte) 0;
		return new byte[] { val };
	}
	
}
