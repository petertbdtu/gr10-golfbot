package blackboard;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import mapping.LidarScan;
import objects.LidarSample;
import objects.Point;
import objects.Pose;

public class BLBallDetector {
	
    public static void main(String args[])
    {
    	//Point a = new Point(1.5f,1.75f);
    	//Point b = new Point(2,2);
    	//Point c = new Point(2.5f,1.75f);
    	//Point center = findCenter(a,b,c);
    	
    	//System.out.println(String.format("Center: x='%f' y='%f'", center.x, center.y));
    	
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    	LidarSample lsa = new LidarSample(0, 10);
    	LidarSample lsb = new LidarSample(90, 10);
    	LidarSample lsc = new LidarSample(180, 10);
    	LidarSample lsd = new LidarSample(270, 10);
    	LidarSample lse = new LidarSample(10, 3);
    	LidarSample lsf = new LidarSample(20, 4);
    	LidarSample lsg = new LidarSample(30, 5);
    	
    	LidarScan scan = new LidarScan();
    	scan.addSample(lsa);
    	scan.addSample(lsb);
    	scan.addSample(lsc);
    	scan.addSample(lsd);
    	scan.addSample(lse);
    	scan.addSample(lsf);
    	scan.addSample(lsg);
    	
    	Mat map = scan.getMat();
    	Imgcodecs.imwrite("C:\\Users\\PeterTB\\Desktop\\map.png", map);
    	System.out.println("Wrote image to PeterTB's desktop.");
    }
    
    public static Point findCenter(Point a, Point b, Point c)
    {
        float k1 = (a.y - b.y) / (a.x - b.x); //Two-point slope equation
        float k2 = (a.y - c.y) / (a.x - c.x); //Same for the (A,C) pair
        Point midAB = new Point((a.x + b.x) / 2, (a.y + b.y) / 2); //Midpoint formula
        Point midAC = new Point((a.x + c.x) / 2, (a.y + c.y) / 2); //Same for the (A,C) pair
        k1 = -1*k1; //If two lines are perpendicular, then the product of their slopes is -1.
        k2 = -1*k2; //Same for the other slope
        float n1 = midAB.y - k1*midAB.x; //Determining the n element
        float n2 = midAC.y - k2*midAC.y; //Same for (A,C) pair
        //Solve y1=y2 for y1=k1*x1 + n1 and y2=k2*x2 + n2
        float x = (n2-n1) / (k1-k2);
        float y = k1*x + n1;
        return new Point(0,0); //return new Point(x, y);
    }
    
    public boolean checkForBalls(LidarScan scan) {
  	
    	return false;
    }
    
    public Pose getClosestBall(LidarScan scan) {
    	
    	return null;
    }
    
    public boolean ballAtPose(Pose pose) {
    	
    	return false;
    }
}
