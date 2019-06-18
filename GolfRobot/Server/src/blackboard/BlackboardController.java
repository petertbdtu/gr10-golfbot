package blackboard;

import java.util.ArrayList;
import java.util.LinkedList;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import communication.CameraReceiver;
import communication.LegoReceiver;
import communication.LidarReceiver;
import gui.ServerGUI;
import mapping.LidarScan;
import mapping.Vision;

public class BlackboardController extends Thread {
	private volatile ArrayList<BlackboardListener> bbListeners;
	private volatile LinkedList<BlackboardListener> bbNewListeners;

	private CameraReceiver camera;
	private LegoReceiver lego;
	private LidarReceiver lidar;
	private ServerGUI gui;

	private volatile BlackboardSample bbSample;
	private volatile boolean stopBlackboard = false;
	private long cycle = 0;
	
	public BlackboardController(ServerGUI gui) {
		this.gui = gui;
		this.bbListeners = new ArrayList<BlackboardListener>();
		this.bbNewListeners = new LinkedList<BlackboardListener>();
	}
	
	public synchronized void addCameraReceiver(CameraReceiver camera) {
		this.camera = camera;
	}
	
	public synchronized void addLegoReceiver(LegoReceiver lego) {
		this.lego = lego;
	}
	
	public synchronized void addLidarReceiver(LidarReceiver lidar) {
		this.lidar = lidar;
	}
	
	public synchronized void registerListener(BlackboardListener bbListener) {
		bbNewListeners.add(bbListener);
	}
	
	@Override
	public void run() {
		while(!stopBlackboard) {
			newBlackboardValues();
			notifyListeners();
			checkForNewListeners();
		}
	}
	
	private synchronized void checkForNewListeners() {
		while(!bbNewListeners.isEmpty())
			bbListeners.add(bbNewListeners.pop());
	}

	private void notifyListeners() {
		for (BlackboardListener listener : bbListeners) {
			listener.blackboardUpdated(bbSample);
		}
	}
	
	private void newBlackboardValues() {
		bbSample = new BlackboardSample();
		bbSample.cycle = cycle++;
//	
//		if(camera != null) {
//			bbSample.frame = camera.getImage();
//			if (bbSample.frame.length > 20) {
//			gui.setIsCollecting(Vision.detectBallInImage(bbSample.frame) + "");
//			gui.setCameraFrame(bbSample.frame);
//			}
//		}
		
		if(lego != null) {
			bbSample.isMoving = lego.getIsMoving();
			gui.setIsMoving(bbSample.isMoving + "");
			bbSample.isCollecting = lego.getIsCollecting();
			gui.setIsCollecting(bbSample.isCollecting + "");
		} else {
			bbSample.isMoving = false;
			gui.setIsMoving(false + "");
			bbSample.isCollecting = false;
			gui.setIsCollecting(false + "");

		}
		
		if(lidar != null) {
			bbSample.scan = lidar.getScan();
			if(bbSample.scan.scanSize() > 0) {
				try {					
					Mat mat = Vision.scanToLineMap(bbSample.scan);
					Mat map = mat.clone();
					Mat obstacles = new Mat(map.size(), map.type());
					
					//Remove shit obstacles
					Vision.findWallsAndRemove(map, obstacles);
					
					// Circles plz
					Vision.drawCirclesOnMap(mat, Vision.findAllBallsLidar(map));
					
					//draw in le GUI
					Vision.drawRobotMarker(mat);

					Rect roi = new Rect(1000, 1000, 2000, 2000);
					mat = new Mat(mat, roi);
					obstacles = new Mat(obstacles, roi);
					
					LidarScan frontScans = BLStateController.getFrontScans(bbSample.scan);
					Mat frontDirectionMap = Vision.scanToLineMap(frontScans);
					Vision.drawCirclesOnMap(frontDirectionMap, Vision.findAllBallsLidar(frontDirectionMap));
					Vision.drawRobotMarker(frontDirectionMap);
					frontDirectionMap = new Mat(frontDirectionMap, roi);

					
					gui.setLidarScan(Vision.matToImageBuffer(mat));
					gui.setCameraFrame(Vision.matToImageBuffer(frontDirectionMap));
					
					//Memory ?????????
					map.release();
					obstacles.release();
					mat.release();
					frontDirectionMap.release();
					
				} catch (Exception e) { }
			}
		} else {
			bbSample.scan = new LidarScan();
		}
	}

	public void stopBlackboard() {
		stopBlackboard = true;
	}

	public void removeListener(BlackboardListener bbListener) {
		bbListeners.remove(bbListener);
	}
}
