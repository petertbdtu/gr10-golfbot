package golfbot.robot;

import golfbot.robot.knowledgesources.KSBallManagement;
import golfbot.robot.knowledgesources.KSLocation;
import golfbot.robot.knowledgesources.KSNavigation;
import golfbot.robot.navigation.GyroPoseProvider;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class RobotSingle {
	private static double offset = 100; //distance between the two wheels divided by 2 in mm
	private static double wheelDiamater = 30; //Diameter of wheels in mm
	private static Wheel leftWheel = WheeledChassis.modelWheel(Motor.A, wheelDiamater).offset(offset);
	private static Wheel rightWheel = WheeledChassis.modelWheel(Motor.B, wheelDiamater).offset(-offset);
	private static Chassis chassis = new WheeledChassis(new Wheel[] {leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
	public static KSNavigation navigation = new KSNavigation();
	public static MovePilot pilot = new MovePilot(chassis);
	public static GyroPoseProvider localization = new GyroPoseProvider(pilot);
	public static KSBallManagement manager = new KSBallManagement();
	public static KSLocation location;
	public static CommandReceiver receiver;
	
	public static void main(String [ ] args) {
		location = new KSLocation(SensorPort.S1);
		navigation.run();
		location.run();
		manager.run();
		receiver = new CommandReceiver(navigation, manager);
		receiver.run();
	}
}
