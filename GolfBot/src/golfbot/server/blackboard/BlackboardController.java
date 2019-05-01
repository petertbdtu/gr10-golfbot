package golfbot.server.blackboard;

import java.util.ArrayList;

import golfbot.server.communication.CameraReceiver;
import golfbot.server.communication.LegoReceiver;
import golfbot.server.communication.LidarReceiver;

public class BlackboardController extends Thread {
	private ArrayList<BlackboardListener> bbListeners;
	private CameraReceiver camera;
	private LegoReceiver lego;
	private LidarReceiver lidar;

	private BlackboardSample bSample;
	private long cycle = 0;
	
	public BlackboardController(CameraReceiver camera, LegoReceiver lego, LidarReceiver lidar) {
		this.camera = camera;
		this.lego = lego;
		this.lidar = lidar;
		bbListeners = new ArrayList<BlackboardListener>();
	}
	
	public void registerListener(BlackboardListener bbListener) {
		bbListeners.add(bbListener);
	}
	
	@Override
	public void run() {
		while(!bbListeners.isEmpty()) {
			newBlackboardValues();
			notifyListeners(bSample);
		}
	}
	
	private void notifyListeners(BlackboardSample bs) {
		for (BlackboardListener bl : bbListeners) {
			bl.blackboardUpdated(bs);
		}
	}
	
	private void newBlackboardValues() {
		bSample = new BlackboardSample();
		bSample.cycle = cycle;
		bSample.isMoving = lego.getIsMoving();
		bSample.isCollecting = lego.getIsCollecting();
		bSample.robotPose = lego.getPose();
		bSample.scan = lidar.getScan();
		
		cycle += 1;
	}
}
