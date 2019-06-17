package blackboard;

import java.util.ArrayList;

import communication.CameraReceiver;
import communication.LegoReceiver;
import communication.LidarReceiver;
import mapping.LidarAnalyser;
import mapping.LidarScan;
import objects.Point;

public class BlackboardController extends Thread {
	private ArrayList<BlackboardListener> bbListeners;
	
	private CameraReceiver camera;
	private LegoReceiver lego;
	private LidarAnalyser lidar;

	private BlackboardSample bbSample;
	private volatile boolean stopBlackboard = false;
	private long cycle = 0;
	
	public BlackboardController(CameraReceiver camera, LegoReceiver lego, LidarAnalyser lidar) {
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
		while(!stopBlackboard) {
			newBlackboardValues();
			notifyListeners(bbSample);
		}
	}
	
	private void notifyListeners(BlackboardSample bs) {
		for (BlackboardListener bl : bbListeners) {
			bl.blackboardUpdated(bs);
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
			bbSample.isCollecting = lego.getIsCollecting();
		} else {
			bbSample.isMoving = false;
			bbSample.isCollecting = false;
		}
		
		if(lidar != null) {
			bbSample.scan = lidar.getScan();
			bbSample.balls = lidar.getBalls();
			bbSample.obstacles = lidar.getObstacles();
			bbSample.goal = lidar.getGoal();
		} else {
			bbSample.scan = new LidarScan();
			bbSample.balls = new ArrayList<Point>();
			bbSample.obstacles = new ArrayList<Point>();
			bbSample.goal = new Point(0,0);
		}
	}

	public void stopBlackboard() {
		stopBlackboard = true;
	}

	public void removeListener(BlackboardListener bbListener) {
		bbListeners.remove(bbListener);
		
	}
}
