package golfbot.navigation;

import golfbot.robot.knowledgesources.KSMotor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;

public class MotorController {
	
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private KSMotor ksMotors;
	
	private final int TACHOS_PR_DEGREE = 20;			//test to find the angle
	
	/**
	 * Constructer for MotorController
	 * @param leftMotorPort port for the left motor
	 * @param rightMotorPort port for the right motor
	 * @param curPos current position of the golfbot
	 */
	public MotorController (Port leftMotorPort, Port rightMotorPort) {
		leftMotor = new EV3LargeRegulatedMotor(leftMotorPort);
		rightMotor = new EV3LargeRegulatedMotor(rightMotorPort);
		ksMotors = new KSMotor(leftMotorPort, rightMotorPort);
	}
	
	/**
	 * Go forward a specified distance
	 * @param distance distance in tachos
	 */
	public void forward(int distance) {
		/*
		//Set speed and acceleration
		 */
		leftMotor.synchronizeWith(new RegulatedMotor[] { rightMotor });
		leftMotor.startSynchronization();
		leftMotor.rotateTo(distance/TACHOS_PR_DEGREE);
		rightMotor.rotateTo(distance/TACHOS_PR_DEGREE);
		leftMotor.waitComplete();
		leftMotor.endSynchronization();
	}
	
	
	/**
	 * Private function to operate the EV3LargeRegulatedMotor class, low-level
	 * @param direction true for left, false for right
	 * @param angle angle in degrees
	 */
	public void turn(int angle) {
		leftMotor.startSynchronization();
		leftMotor.rotateTo(angle);
		rightMotor.rotateTo(angle);
		leftMotor.waitComplete();
		leftMotor.endSynchronization();
	}
	
	
}
