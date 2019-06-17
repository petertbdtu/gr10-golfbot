package blackboard;

import java.util.ArrayList;
import java.util.List;

import mapping.LidarScan;
import objects.Point;

public class BlackboardSample {
	public long cycle;
	public LidarScan scan;
	public volatile boolean isMoving;
	public volatile boolean isCollecting;
	public List<Point> balls;
	public Point goal;
	public List<Point> obstacles;
	
	public BlackboardSample(BlackboardSample bbSample) {
		this.cycle = bbSample.cycle;
		this.scan = new LidarScan(bbSample.scan);
		this.balls = new ArrayList<Point>(bbSample.balls);
		this.obstacles = new ArrayList<Point>(bbSample.obstacles);
		this.goal = new Point(bbSample.goal);
		this.isMoving = bbSample.isMoving;
		this.isCollecting = bbSample.isCollecting;
	}

	public BlackboardSample() {
	}
}
