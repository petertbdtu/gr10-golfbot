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
	private volatile List<Point> obstacles;
	private volatile List<Point> balls;
	private volatile Point goal;
	private volatile LidarScan scan;
	
	public LidarAnalyser(LidarReceiver lidarReceiver, ServerGUI serverGUI) {
		this.lidarReceiver = lidarReceiver;
		this.serverGUI = serverGUI;
		this.obstacles = new ArrayList<Point>();
		this.balls = new ArrayList<Point>();
		this.scan = new LidarScan();
		this.goal = new Point(0,0);
	}
	
	@Override
	public void run() {
		setScan(new LidarScan());
		while(keepAlive) {
			LidarScan newScan = lidarReceiver.getScan();
			if(newScan != null) {
				if(newScan.scanSize() != scan.scanSize()) {
					analyseData(newScan);
					scan = new LidarScan(newScan);
				}
			}
		}
	}
	
	private void analyseData(LidarScan scan) {
		try {
			// Create image yes yes yes
			Mat map = Vision.scanToPointMap(scan);
			Mat obstacles = new Mat(map.size(), map.type());
			
			//Remove shit obstacles
			Vision.findWallsAndRemove(map, obstacles);
			
			//Very nice obstacles
			this.obstacles = Vision.collisionMapToPoints(obstacles);
			
			// Circles plz
			//Vision.drawMoreLinesOnMap(map);
			Mat circles = Vision.findAllBallsLidar(map);
			setBalls(Vision.getCircleLocsFromMat(circles));
			map = Vision.drawCirclesOnMap(map, circles);
			
			//draw in le GUI
			serverGUI.setLidarScan(Vision.matToImageBuffer(map));
			serverGUI.setCamera(Vision.matToImageBuffer(obstacles));
		} catch (Exception e) { e.printStackTrace(); }
	}

	public List<Point> getObstacles() {
		return new ArrayList<Point>(obstacles);
	}

	public void setObstacles(List<Point> obstacles) {
		this.obstacles = obstacles;
	}

	public List<Point> getBalls() {
		return new ArrayList<Point>(balls);
	}

	public void setBalls(List<Point> balls) {
		this.balls = balls;
	}

	public Point getGoal() {
		return new Point(goal);
	}

	public void setGoal(Point goal) {
		this.goal = goal;
	}
	
	public LidarScan getScan() {
		return new LidarScan(scan);
	}

	public void setScan(LidarScan scan) {
		this.scan = scan;
	}
	
	
}
