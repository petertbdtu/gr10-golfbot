package blackboard;

import mapping.LidarScan;
import objects.Pose;

public class BlackboardSample {
	public Long cycle;
	public LidarScan scan;
	public boolean isMoving;
	public boolean isCollecting;
	public Pose robotPose;
}
