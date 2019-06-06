package blackboard;

import mapping.LidarScan;
import objects.Pose;

public class BlackboardSample {
	public long cycle;
	public LidarScan scan;
	public boolean isMoving;
	public boolean isCollecting;
	public Pose robotPose;
	
	public BlackboardSample(BlackboardSample bbSample) {
		this.cycle = bbSample.cycle;
		this.scan = new LidarScan(bbSample.scan);
		this.isMoving = bbSample.isMoving;
		this.isCollecting = bbSample.isCollecting;
		this.robotPose = new Pose(bbSample.robotPose);
	}

	public BlackboardSample() {
	}
}
