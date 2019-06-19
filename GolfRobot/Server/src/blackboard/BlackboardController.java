package blackboard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import communication.LegoReceiver;
import communication.LidarReceiver;
import deprecated.CameraReceiver;
import gui.ServerGUI;
import mapping.LidarScan;
import mapping.Vision;
import objects.LidarSample;
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
//					LidarScan fewerAngles = new LidarScan();
//					for(LidarSample s : bbSample.scan.getSamples()) {
//						if(s.angle > 45.0 && s.angle < 115.0 && s.distance < 1000)
//							fewerAngles.addSample(s);
//					}
					
					Mat map = Vision.scanToLineMap(bbSample.scan);
//					Mat print = new Mat();
					//gui.setLidarScan(Vision.matToImageBuffer(mat));
				//	mat.release();
//					
//					Mat map = Vision.scanToLineMap(scan);
//					Mat mapToShow = new Mat();
//					Imgproc.cvtColor(map, mapToShow, Imgproc.COLOR_GRAY2BGR); // ALLOW COLORS
//					Imgproc.cvtColor(map, print, Imgproc.COLOR_GRAY2BGR); // ALLOW COLORS

					// Masks
//					Mat blackMat = Mat.zeros(map.size(), map.type());
//					Mat redMat = mapToShow.clone().setTo(new Scalar(0,0,255));
					
					// obstacles
//					Mat obstacles = Vision.findWalls(map);
//					redMat.copyTo(mapToShow, obstacles);
						
					// Goal
//					Point goal = Vision.findGoal(map, print);
//					if (goal != null)  Vision.drawGoalPoint(mapToShow, goal);
					
					// Send to GUI
					//gui.setCameraFrame(Vision.matToImageBuffer(mapToShow));
					gui.setLidarScan(Vision.matToImageBuffer(map));
					
					// Memory fix
//					map.release();
//					mapToShow.release();
//					blackMat.release();
//					redMat.release();
				} catch (Exception e) { e.printStackTrace(); }
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
