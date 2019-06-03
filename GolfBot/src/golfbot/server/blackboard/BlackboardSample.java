package golfbot.server.blackboard;

import golfbot.server.utilities.LidarScan;
import lejos.robotics.navigation.Pose;

public class BlackboardSample {
	public Long cycle;
	public LidarScan scan;
	public boolean isMoving;
	public boolean isCollecting;
	public Pose robotPose;
}
