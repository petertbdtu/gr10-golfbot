package blackboard;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

public class BLCollisionDetector extends Thread implements BlackboardListener {

	//TODO get real dimensions for robot, areas
	private boolean isMoving = false;
	private int robotWidth = 100; //				mm
	private int distanceFromLidarToFront = 33; //	mm
	private int wantedDistanceFromWall = 10; //		mm
	private Rectangle avoidanceArea = new Rectangle(-(robotWidth/2) - wantedDistanceFromWall, distanceFromLidarToFront + wantedDistanceFromWall, robotWidth + (wantedDistanceFromWall*2), + (wantedDistanceFromWall*2));
	private Rectangle warningArea = new Rectangle();
	List<objects.Point> list;
	private BlackboardSample bbSample;
	
	public void StartAvoidance() {
		   list = bbSample.scan.getPoints();
		   for(int i = 0; i < list.size(); i++) {
			   Point point = list.get(i);
			   if(avoidanceArea.contains(point)) {
				   //TODO make the robot stop
				   //Method call
			   } else if(warningArea.contains(point)) {
				   //TODO slow down the robot
				   //Method call
			   }
		   }
	}

	@Override
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = new BlackboardSample(bbSample);
	}
	
	@Override
	public void run() {
		int cycle = -1;
		while(true) {
			if(bbSample.cycle == cycle + 1) {
				StartAvoidance();
				cycle++;
			}
		}
	}
}
