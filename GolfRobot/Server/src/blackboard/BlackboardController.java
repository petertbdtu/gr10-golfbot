package blackboard;

import java.util.ArrayList;
import java.util.LinkedList;
import org.opencv.core.Mat;

import communication.LegoReceiver;
import communication.LidarReceiver;
import gui.ServerGUI;
import mapping.LidarScan;
import mapping.Vision;

public class BlackboardController extends Thread {
	private volatile ArrayList<BlackboardListener> bbListeners;
	private volatile LinkedList<BlackboardListener> bbNewListeners;

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
		
		if(lego != null) {
			bbSample.isMoving = lego.getIsMoving();
			gui.setIsMoving(bbSample.isMoving + "");
			bbSample.isCollecting = lego.getIsCollecting();
			gui.setGoalFinding(bbSample.isCollecting + "");
		} else {
			bbSample.isMoving = false;
			gui.setIsMoving(false + "");
			bbSample.isCollecting = false;
			gui.setGoalFinding(false + "");

		}
		
		if(lidar != null) {
			bbSample.scan = lidar.getScan();
			if(bbSample.scan.scanSize() > 0) {
				try {	
					Mat map = Vision.scanToLineMap(bbSample.scan);
					gui.setLidarScan(Vision.matToImageBuffer(map));
					map.release();
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
