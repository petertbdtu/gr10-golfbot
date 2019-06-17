package blackboard;

import java.util.ArrayList;
import java.util.LinkedList;

import org.opencv.core.Mat;

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
		
		if(camera != null) {
			//TODO ADD FRAME???
		}
		
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
					Mat map = Vision.scanToPointMap(bbSample.scan);
//					System.out.println(map.size());
//					
//					Mat obstacles = new Mat(map.size(), map.type());
//					
//					//Remove shit obstacles
//					Vision.findWallsAndRemove(map, obstacles);
//					System.out.println(map.size());
//					
//					// Circles plz
//					Vision.drawCirclesOnMap(map, Vision.findAllBallsLidar(map));
//					System.out.println(map.size());
//
//					
//					//draw in le GUI
					gui.setLidarScan(Vision.matToImageBuffer(map));
//					gui.setLidarAnalyzedScan(Vision.matToImageBuffer(obstacles));
					
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
