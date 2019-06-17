package blackboard;


import mapping.LidarScan;

public class BlackboardSample {
	public long cycle;
	public volatile LidarScan scan;
	public volatile boolean isMoving;
	public volatile boolean isCollecting;
	
	public BlackboardSample(BlackboardSample bbSample) {
		this.cycle = bbSample.cycle;
		this.scan = new LidarScan(bbSample.scan);
		this.isMoving = bbSample.isMoving;
		this.isCollecting = bbSample.isCollecting;
	}

	public BlackboardSample() { }
}
