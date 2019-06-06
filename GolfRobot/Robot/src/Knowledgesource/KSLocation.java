package Knowledgesource;

import java.nio.ByteBuffer;

import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;
import robot.GyroPoseProvider;
import robot.PoseSample;

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

	@Override
	protected byte[] getKnowledgeAsBytes() {
		Pose curPose = provider.getPose();
		ByteBuffer bf = ByteBuffer.allocate(12);
		bf.putInt((int) curPose.getX());
		bf.putInt((int) curPose.getY());
		bf.putFloat(curPose.getHeading());
		return bf.array();
	}
}
