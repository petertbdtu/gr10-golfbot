package blackboard;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import communication.CommandTransmitter;

public class BLCollisionDetector extends Thread implements BlackboardListener {

	//TODO get real dimensions for robot
	private int robotWidth = 160; //				mm
	private int distanceFromLidarToFront = 33; //	mm
	private int wantedDistanceFromWall = 20; //		mm
	private Rectangle avoidanceArea = new Rectangle(-(robotWidth/2) - wantedDistanceFromWall, distanceFromLidarToFront + wantedDistanceFromWall, robotWidth + (wantedDistanceFromWall*2), + (wantedDistanceFromWall*2));
	private Rectangle warningArea = new Rectangle(avoidanceArea.x, avoidanceArea.y + avoidanceArea.height, avoidanceArea.width, avoidanceArea.height);
	List<objects.Point> list;
	private BlackboardSample bbSample;
	private CommandTransmitter transmitter;
	private boolean detected = false;
	
	public BLCollisionDetector(CommandTransmitter transmitter, boolean detected) {
		this.transmitter = transmitter;
		this.detected = detected;
	}
	
	public void StartAvoidance() {
		   list = bbSample.scan.getPoints();
		   for(int i = 0; i < list.size(); i++) {
			   Point point = list.get(i);
			   if(avoidanceArea.contains(point)) {
				   transmitter.robotStop();
				   detected = true;
			   } else if(warningArea.contains(point)) {
				   transmitter.robotSlowDown();
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
