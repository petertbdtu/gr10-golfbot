package golfbot.navigation;

import lejos.hardware.motor.Motor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class Navigation {
	
	double offset = 100; //distance between the two wheels divided by 2 in mm
	double wheelDiamater = 30; //Diameter of wheels in mm
	Wheel leftWheel = WheeledChassis.modelWheel(Motor.A, wheelDiamater).offset(offset);
	Wheel rightWheel = WheeledChassis.modelWheel(Motor.B, wheelDiamater).offset(-offset);
	Chassis chassis = new WheeledChassis(new Wheel[] {leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
	
	MovePilot pilot = new MovePilot(chassis);
	
	public MovePilot getInstance() {
		return pilot;
	}
	
	public void travelTo(double angle, double distance) {
		pilot.setLinearSpeed(5000.00);
		pilot.rotate(angle);
		pilot.travel(distance);
	}
	
	public void stopMoving() {
		pilot.stop();
	}
	
}
