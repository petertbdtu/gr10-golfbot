package robot;


import Knowledgesource.KSBallManagement;
import Knowledgesource.KSNavigation;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class RobotMain {
	public static void main(String [ ] args) {
		// Server IP hello
		String ip = "172.20.10.2";
		int portSend = 3000;
		int portReceive = 3001;
		
		// Really important bool
		boolean YesRobotRunYesYes = true;
		
		// Build Chassis
		double offset = 78; //distance between the two wheels divided by 2 in mm
		double wheelDiamater = 30; //Diameter of wheels in mm
		Wheel leftWheel = WheeledChassis.modelWheel(new EV3LargeRegulatedMotor(MotorPort.A), wheelDiamater).offset(offset);
		Wheel rightWheel = WheeledChassis.modelWheel(new EV3LargeRegulatedMotor(MotorPort.B), wheelDiamater).offset(-offset);
		Chassis chassis = new WheeledChassis(new Wheel[] {leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		// Build Navigation
		MovePilot pilot = new MovePilot(chassis);
		KSNavigation navigation = new KSNavigation(pilot);
		if(YesRobotRunYesYes)
			YesRobotRunYesYes = navigation.connect(ip, portSend);
	
		// Build ball management
		KSBallManagement ballManager = new KSBallManagement();
		if(YesRobotRunYesYes)
			YesRobotRunYesYes = ballManager.connect(ip, portSend);

		// Command Receiver
		CommandReceiver receiver = new CommandReceiver(navigation, ballManager);
		if(YesRobotRunYesYes && receiver.connect(ip, portReceive))
			receiver.start();
		else
			YesRobotRunYesYes = false;
		
		// Wait until receiver is dead
		if(YesRobotRunYesYes) {
			try { receiver.join(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}
