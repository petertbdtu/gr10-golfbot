package blackboard;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import communication.CommandTransmitter;

public class BLCollisionDetector extends Thread implements BlackboardListener {

	private boolean newData;
	
	int ncords = 5;

	int xcords[] = new int[ncords]; 
	int ycords[] = new int[ncords];

	private final int ROBOT_WIDTH = 160;
	private final int ROBOT_FRONT_LENGTH = 160;
	private final int ROBOT_BACK_LENGTH = 80;
	private final int ROBOT_FRONT_TIP = 200;
	
	objects.Point offset = new objects.Point(0,30);

	List<objects.Point> listArea = new ArrayList<objects.Point>();

	Polygon collisionHull;

	AffineTransform trans;
		
	private BlackboardSample bbSample;
	public volatile boolean isDetected = false;
	
	public BLCollisionDetector(objects.Point p, List<objects.Point> areaToCheck){
		  listArea = areaToCheck;
		  buildCoordsFromOrigin();
		  //trans.rotate(Math.toRadians(10),collisionHull.xpoints[0],collisionHull.ypoints[0]);
		  collisionHull = new Polygon(xcords, ycords, ncords);
		  
	}
	
	public AffineTransform buildTransform(){
		return new AffineTransform();
	}
	
	private void buildCoordsFromOrigin(){
		xcords[0] = offset.x;
		ycords[0] = offset.y + ROBOT_FRONT_TIP;

		xcords[1] = offset.x - (ROBOT_WIDTH/2);
		ycords[1] = offset.y + (ROBOT_FRONT_LENGTH);

		xcords[2] = offset.x - (ROBOT_WIDTH/2);
		ycords[2] = offset.y - (ROBOT_BACK_LENGTH);

		xcords[3] = offset.x + (ROBOT_WIDTH/2);
		ycords[3] = offset.y - (ROBOT_BACK_LENGTH);

		xcords[4] = offset.x + (ROBOT_WIDTH/2);
		ycords[4] = offset.y + (ROBOT_FRONT_LENGTH);
	}
	
	public void checkForCollision() {
	   listArea = bbSample.scan.getPoints();
	   for (objects.Point point : listArea) {
		   if(collisionHull.contains(point)) {
			   isDetected = true;
			   break;
		   }
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
