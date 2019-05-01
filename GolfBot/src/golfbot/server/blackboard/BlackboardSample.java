package golfbot.server.blackboard;

import java.util.HashMap;

import lejos.robotics.navigation.Pose;

public class BlackboardSample {
	public Long cycle;
	public HashMap<Double,Double> scan;
	public boolean isMoving;
	public boolean isCollecting;
	public Pose robotPose;
}
