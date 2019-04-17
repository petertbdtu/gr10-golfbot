package golfbot.robot.knowledgesources;

import golfbot.navigation.GyroPoseProvider;
import golfbot.robot.RobotSingle;
import golfbot.samples.PoseSample;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;

public class KSLocation extends KnowledgeSource<PoseSample> {
	
	Pose currentPose;
	GyroPoseProvider provider = RobotSingle.localization;
	
	private EV3GyroSensor gyro;
	
	public KSLocation (Port port) {
		this.gyro = new EV3GyroSensor(port);
	}
	
	
	
	public Pose updateLocation(MovePilot pilot) {
		while(pilot.isMoving()) {
			currentPose = provider.getPose();
		}
		return currentPose;
	}

	@Override
	protected PoseSample getKnowledge() {
		return new PoseSample(provider.getPose());
	}
}
