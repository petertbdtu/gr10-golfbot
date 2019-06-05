package Navigation;

import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;

public class KSLocation extends KnowledgeSource<PoseSample> {
	
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
	protected PoseSample getKnowledge() {
		Pose curPose = provider.getPose();
		return new PoseSample(curPose.getX(), curPose.getY(), curPose.getHeading());
	}
}
