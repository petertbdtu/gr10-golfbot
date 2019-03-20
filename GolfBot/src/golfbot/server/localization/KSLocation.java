package golfbot.server.localization;

import golfbot.navigation.GyroPoseProvider;
import golfbot.robot.knowledgesources.KSGyro;
import golfbot.robot.knowledgesources.KnowledgeSource;
import golfbot.samples.GyroSample;
import golfbot.samples.PoseSample;
import golfbot.server.blackboard.Blackboard;
import golfbot.server.blackboard.BlackboardController;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.Gyroscope;
import lejos.robotics.GyroscopeAdapter;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.localization.CompassPoseProvider;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;
import lejos.nxt.addon.GyroSensor;
import lejos.utility.GyroDirectionFinder;

public class KSLocation extends KnowledgeSource<PoseSample> {
	
	Pose currentPose;
	
	//NavigationPilot pilot; //Get reference to the NavigationPilot and pass it here
	
	private EV3GyroSensor gyro;
	
	public KSLocation (Port port) {
		this.gyro = new EV3GyroSensor(port);
	}
	
	
	
	public Pose updateLocation(MovePilot pilot) {
		while(pilot.isMoving()) {
			GyroPoseProvider provider = new GyroPoseProvider(pilot);
			currentPose = provider.getPose();
		}
		return currentPose;
	}

	@Override
	protected PoseSample getKnowledge() {
		return null; // return updateLocation(pilot);
	}
}
