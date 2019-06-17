package mapping;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import communication.LidarReceiver;
import gui.ServerGUI;
import objects.Point;

public class LidarAnalyser extends Thread {

	public volatile boolean keepAlive = true;
	private LidarReceiver lidarReceiver;
	private ServerGUI serverGUI;
	private List<Point> obstacles;
	private List<Point> balls;
	private Point goal;
	private LidarScan scan;
	
	public LidarAnalyser(LidarReceiver lidarReceiver, ServerGUI serverGUI) {
		this.lidarReceiver = lidarReceiver;
		this.serverGUI = serverGUI;
	}
	
	@Override
	public void run() {
		setScan(new LidarScan());
		while(keepAlive) {
			LidarScan newScan = lidarReceiver.getScan();
			if(newScan != null) {
				if(newScan.scanSize() != getScan().scanSize()) {
					analyseData(newScan);
					setScan(new LidarScan(newScan));
				}
			}
		}
	}
	
	private void analyseData(LidarScan scan) {
		try {
			Mat map = Vision.scanToMap(scan);
			Mat obstacles = new Mat(map.size(), map.type());
			Vision.findWallsAndRemove(map, obstacles);
			List<Point> lidarMap = Vision.
			Mat circles = Vision.findAllBallsLidar(map);
			setBalls(Vision.getCircleLocsFromMat(circles));
			map = Vision.drawCirclesOnMap(map, circles);
			serverGUI.setLidarScan(Vision.matToImageBuffer(map));
			serverGUI.setCamera(Vision.matToImageBuffer(obstacles));
		} catch (Exception e) { e.printStackTrace(); }
	}

	public synchronized List<Point> getObstacles() {
		return new ArrayList<Point>(obstacles);
	}

	public synchronized void setObstacles(List<Point> obstacles) {
		this.obstacles = obstacles;
	}

	public synchronized List<Point> getBalls() {
		return new ArrayList<Point>(balls);
	}

	public synchronized void setBalls(List<Point> balls) {
		this.balls = balls;
	}

	public synchronized Point getGoal() {
		return new Point(goal);
	}

	public synchronized void setGoal(Point goal) {
		this.goal = goal;
	}
	
	public synchronized LidarScan getScan() {
		return new LidarScan(scan);
	}

	public synchronized void setScan(LidarScan scan) {
		this.scan = scan;
	}
	
	
}
