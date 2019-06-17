package blackboard;

import java.util.ArrayList;
import java.util.LinkedList;

import communication.CameraReceiver;
import communication.LegoReceiver;
import communication.LidarReceiver;
import gui.ServerGUI;
import mapping.LidarAnalyser;
import mapping.LidarScan;
import mapping.Vision;
import objects.Point;

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
	
	public BlackboardController(CameraReceiver camera, LegoReceiver lego, LidarReceiver lidar, ServerGUI gui) {
		this.camera = camera;
		this.lego = lego;
		this.lidar = lidar;
		this.gui = gui;
		this.bbListeners = new ArrayList<BlackboardListener>();
		this.bbNewListeners = new LinkedList<BlackboardListener>();
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
			gui.setLidarScan(Vision.matToImageBuffer(Vision.scanToPointMap(bbSample.scan)));
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
