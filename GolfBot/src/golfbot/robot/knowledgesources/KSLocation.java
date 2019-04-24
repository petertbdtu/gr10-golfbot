package golfbot.robot.knowledgesources;

import golfbot.robot.navigation.GyroPoseProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;

public class KSLocation extends KnowledgeSource<Pose> {
	
	Pose currentPose;
	GyroPoseProvider provider;
	
	
	public KSLocation(GyroPoseProvider provider) {
		this.provider = provider;	
	}
	
	public Pose updateLocation(MovePilot pilot) {
		while(pilot.isMoving()) {
			currentPose = provider.getPose();
		}
		return currentPose;
	}

	@Override
	protected Pose getKnowledge() {
		return provider.getPose();
	}
}
