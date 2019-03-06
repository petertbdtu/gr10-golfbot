package golfbot.navigation;

import golfbot.server.objects.Pose;
import lejos.hardware.port.Port;

public class Navigator {

	public Pose curPos;
	public Pose dest;
	
	private MotorController MC;
	
	/**
	 * Constructor for the Navigator
	 * @param leftMotorPort port of the left motor
	 * @param rightMotorPort port of the right motor
	 * @param curPos the golfbots current position
	 */
	public Navigator(Port leftMotorPort, Port rightMotorPort, Pose curPos) {
		MC = new MotorController(leftMotorPort, rightMotorPort);
		this.curPos = curPos;
	}
	
	
	/**
	 * High-level method to drive to a location in the intern map
	 * @param dest destination for the robot
	 */
	public void goToDest(Pose dest) {
		this.dest = dest;
		if(curPos.heading != dest.heading) {
			turnToHeading(dest.heading);
		}
		MC.forward(calculateDistance());
	}
	
	/**
	 * Change heading of the robot, without moving forward or backward
	 * @param heading the desired heading
	 */
	public void turnToHeading(float heading) {
		MC.turn(calculateAngle());
	}
	
	/**
	 * Calculate the distance between curPos and dest
	 * @return the distance in tachos
	 */
	private int calculateDistance() {
		int distance = 0;
		/* ... */
		return distance;
	}
	
	/**
	 * Calculate the angle between curPos and dest
	 * @return the angle in degrees
	 */
	private int calculateAngle() {
		int angle = 0;
		/* ... */
		return angle;
	}
}


