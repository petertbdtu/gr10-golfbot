package blackboard;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import mapping.LidarScan;
import objects.Point;

public class BLCollisionDetector extends Thread implements BlackboardListener {
	
	public volatile boolean keepDetecting = true;
	private volatile boolean isDetected = false;

	private final int ROBOT_WIDTH = 160;
	private final int ROBOT_FRONT_LENGTH = 110;
	private final int ROBOT_BACK_LENGTH = 80;
	private final int ROBOT_FRONT_TIP = 185;
	private final Point offset = new Point(-100,0);
	
	private int ncords = 5;
	private int xcords[] = new int[ncords]; 
	private int ycords[] = new int[ncords];
	
	private BlackboardSample bbSample;
	private LidarScan newObstacles;
	
	private Shape curHull;
	private Polygon collisionHull;
	private Rectangle ballCollectHull;
	
	public BLCollisionDetector(){
		  buildCoordsFromOrigin2();
		  //trans.rotate(Math.toRadians(10),collisionHull.xpoints[0],collisionHull.ypoints[0]);
		  collisionHull = new Polygon(xcords, ycords, ncords);
		  ballCollectHull = new Rectangle(xcords[4],ycords[4],xcords[2]-xcords[4],ycords[3]-ycords[4]); //??? XD
		  curHull = collisionHull;
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
	
	private void buildCoordsFromOrigin2(){
		xcords[0] = offset.x - ROBOT_FRONT_TIP;
		ycords[0] = offset.y;

		xcords[1] = offset.x - (ROBOT_FRONT_LENGTH);
		ycords[1] = offset.y + (ROBOT_WIDTH/2);

		xcords[2] = offset.x + (ROBOT_BACK_LENGTH);
		ycords[2] = offset.y + (ROBOT_WIDTH/2);

		xcords[3] = offset.x + (ROBOT_BACK_LENGTH);
		ycords[3] = offset.y - (ROBOT_WIDTH/2);

		xcords[4] = offset.x + (ROBOT_FRONT_LENGTH);
		ycords[4] = offset.y - (ROBOT_WIDTH/2);
	}
	
	public void checkForCollision() {
		for (objects.Point point : newObstacles.getPoints()) {
			if(curHull.contains(point)) {
				isDetected = true;
				break;
			}
		}
	}
	
	public void swapHull(boolean usePoly) {
		curHull = usePoly ? collisionHull : ballCollectHull;		   
		isDetected = false;
	}
	
	public boolean isDetected() {
		return isDetected;
	}
	
	@Override
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = new BlackboardSample(bbSample);
	}
	
	@Override
	public void run() {	
		LidarScan oldObstacles = new LidarScan();
		while(keepDetecting) {
			if(bbSample != null && bbSample.scan != null) {
				newObstacles = new LidarScan(bbSample.scan);
				while(oldObstacles.scanSize() == newObstacles.scanSize()) {
					newObstacles = new LidarScan(bbSample.scan);
				}
				oldObstacles = newObstacles;
				System.out.println("Checking for collision... ");
				checkForCollision();
				System.out.println("Collision Detection: " + isDetected);
			}
		}
	}

	public void setDetected(boolean detected) {
		isDetected = detected;
	}
}
