package blackboard;

import java.util.ArrayList;

import communication.CameraReceiver;
import communication.LegoReceiver;
import communication.LidarReceiver;

public class BlackboardController extends Thread {
	private ArrayList<BlackboardListener> bbListeners;
	private CameraReceiver camera;
	private LegoReceiver lego;
	private LidarReceiver lidar;

	private BlackboardSample bSample;
	private volatile boolean stopBlackboard = false;
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
		while(!bbListeners.isEmpty() && !stopBlackboard) {
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
		bSample.cycle = cycle++;
		
		if(camera != null) {
			//TODO ADD FRAME???
		}
		
		if(lego != null) {
			bSample.isMoving = lego.getIsMoving();
			bSample.isCollecting = lego.getIsCollecting();
		}
		
		if(lidar != null) {
			bSample.scan = lidar.getScan();
		}
	}

	public void stopBlackboard() {
		stopBlackboard = true;
	}

	public void removeListener(BlackboardListener bbListener) {
		bbListeners.remove(bbListener);
		
	}
}
