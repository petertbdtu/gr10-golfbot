package golfbot.robot;

import golfbot.robot.knowledgesources.KSBallManagement;
import golfbot.robot.knowledgesources.KSLocation;
import golfbot.robot.knowledgesources.KSNavigation;
import golfbot.robot.navigation.GyroPoseProvider;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class RobotSingle {
	public static void main(String [ ] args) {
		// Server IP hello
		String ip = "localhost";
		
		// Really important bool
		boolean YesRobotRunYesYes = true;
		
		// Build Chassis
		double offset = 100; //distance between the two wheels divided by 2 in mm
		double wheelDiamater = 30; //Diameter of wheels in mm
		Wheel leftWheel = WheeledChassis.modelWheel(Motor.A, wheelDiamater).offset(offset);
		Wheel rightWheel = WheeledChassis.modelWheel(Motor.B, wheelDiamater).offset(-offset);
		Chassis chassis = new WheeledChassis(new Wheel[] {leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		// Build Navigation
		MovePilot pilot = new MovePilot(chassis);
		KSNavigation navigation = new KSNavigation(pilot);
		if(YesRobotRunYesYes && navigation.connect(ip, 3000))
			 navigation.run();
		
		// Build Localisation
		GyroPoseProvider gyroPoseProvider = new GyroPoseProvider(pilot, SensorPort.S2);
		KSLocation location = new KSLocation(gyroPoseProvider);
		if(YesRobotRunYesYes && location.connect(ip, 3001))
			location.run();
		else
			YesRobotRunYesYes = false;
	
		// Build ball management
		KSBallManagement ballManager = new KSBallManagement();
		if(YesRobotRunYesYes && ballManager.connect(ip, 3002))
			ballManager.run();
		else
			YesRobotRunYesYes = false;

		// Command Receiver
		CommandReceiver receiver = new CommandReceiver(navigation, ballManager);
		if(YesRobotRunYesYes && receiver.connect(ip, 3003))
			receiver.run();
		else
			YesRobotRunYesYes = false;
		
		// Wait until receiver is dead
		if(YesRobotRunYesYes) {
			try { receiver.join(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}
