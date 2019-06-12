package blackboard;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import communication.CommandTransmitter;

public class BLCollisionDetector extends Thread implements BlackboardListener {

	private boolean newData;
	
	//TODO get real dimensions for robot
	private final int ROBOT_WIDTH = 160; //				mm
	private final int DISTANCE_TO_FRONT = 200; //	mm
	private final int DISTANCE_TO_WALL = 50; //		mm
	
	private Rectangle avoidanceArea;
	
	
	//private Rectangle warningArea = new Rectangle(avoidanceArea.x, avoidanceArea.y + avoidanceArea.height, avoidanceArea.width, avoidanceArea.height);
	
	List<objects.Point> list;
	private BlackboardSample bbSample;
	public boolean isDetected = false;
	public boolean slowDownDetected = false;
	
	public BLCollisionDetector() {
		int y = -(ROBOT_WIDTH/2) - DISTANCE_TO_WALL;
		int x = -(DISTANCE_TO_FRONT + DISTANCE_TO_WALL);
		int width = DISTANCE_TO_WALL;
		int height = ROBOT_WIDTH + (2 * DISTANCE_TO_WALL);
		
		avoidanceArea = new Rectangle(x, y, width, height);
		newData = false;
	}
	
	public boolean getIsDetected() {
		return isDetected;
	}
	
	public boolean getSlowDown() {
		return slowDownDetected;
	}
	
	public void setSlowDown(boolean bool) {
		slowDownDetected = bool;
	}
	
	public void setIsDetected(boolean bool) {
		isDetected = bool;
	}
	
	public void checkForCollision() {
	   list = bbSample.scan.getPoints();
	   for (Point point : list) {
		   if(avoidanceArea.contains(point)) {
			   isDetected = true;
		   } 
//		   else if(warningArea.contains(point)) {
//			   slowDownDetected = true;
//		   }
	   }
	}

	@Override
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = new BlackboardSample(bbSample);
		newData = true;
	}
	
	@Override
	public void run() {		
		while(true) {
			if(newData) {
				checkForCollision();
			}
		}
	}
}
