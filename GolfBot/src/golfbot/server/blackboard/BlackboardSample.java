package golfbot.server.blackboard;

import java.util.HashMap;

import lejos.robotics.navigation.Pose;

public class BlackboardSample {
	public Long cycle;
	public float sTouchPressed;
	public float sSonicDistance;
	public float sIRDistance;
	public float sGyroRate;
	public float sGyroAngle;
	public float mLeftDrivingTacho;
	public float mRightDrivingTacho;
	public HashMap lidarScan;
	public Pose rPose;
}
