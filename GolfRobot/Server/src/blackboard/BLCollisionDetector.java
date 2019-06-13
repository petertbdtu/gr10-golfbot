package blackboard;

import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import mapping.LidarScan;

public class BLCollisionDetector extends Thread implements BlackboardListener {

	private boolean newData;
	
	int ncords = 5;

	int xcords[] = new int[ncords]; 
	int ycords[] = new int[ncords];

	private final int ROBOT_WIDTH = 160;
	private final int ROBOT_FRONT_LENGTH = 160;
	private final int ROBOT_BACK_LENGTH = 80;
	private final int ROBOT_FRONT_TIP = 200;
	
	private LidarScan newScan;
	
	objects.Point offset = new objects.Point(-200,0);

	List<objects.Point> listArea = new ArrayList<objects.Point>();

	Polygon collisionHull;

	AffineTransform trans;
		
	private BlackboardSample bbSample;
	public volatile boolean isDetected = false;
	
	public BLCollisionDetector(){
		  buildCoordsFromOrigin2();
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
	   for (objects.Point point : newScan.getPoints()) {
		   if(collisionHull.contains(point)) {
			   isDetected = true;
			   break;
		   }
	   }
	}
	
	@Override
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = new BlackboardSample(bbSample);
	}
	
	@Override
	public void run() {	
		LidarScan oldScan = new LidarScan();

		while(true) {
			if(bbSample != null && bbSample.scan != null) {
				newScan = new LidarScan(bbSample.scan);
				while(oldScan.scanSize() == newScan.scanSize()) {
					newScan = new LidarScan(bbSample.scan);
				}
				oldScan = newScan;
				checkForCollision();
			}
		}
	}
}
