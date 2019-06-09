package blackboard;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import mapping.LidarScan;
import objects.LidarSample;
import objects.Point;
import objects.Pose;

public class BLBallDetector {
	
    public static void main(String args[])
    {
    	try {
    		BLBallDetector bd = new BLBallDetector();
    		
    		//System.out.println(String.format("Center: x='%f' y='%f'", center.x, center.y));
    		
    		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    		
    		FileInputStream fis = new FileInputStream("testScan2.data");
    		ObjectInputStream ois = new ObjectInputStream(fis);
    		
    		LidarScan scan = (LidarScan) ois.readObject();
    		
    		Mat map = scan.getMat();
    		Imgcodecs.imwrite("map2.png", map);
    		
    		Mat circles = bd.detectBalls(map);
    		Imgcodecs.imwrite("lines2.png", circles);
    		
    		Mat graph = scan.getGraph(1);
    		Imgcodecs.imwrite("graph2.png", graph);
    		
    	}
    	catch (ClassNotFoundException | IOException e) {
    		System.out.println("Could not read test scan data file");
    	}
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
    
    public Mat detectBalls(Mat map) {
    	/*
    	Mat blur = new Mat();
    	
    	double blur_kernel_size = 5;
    	Size ksize = new Size(blur_kernel_size, blur_kernel_size);
    	double sigmaX = 0;
    	Imgproc.GaussianBlur(map, blur, ksize, sigmaX);
    	*/
    	
    	Mat edges = new Mat();
    	double lo_thresh = 50;
    	double hi_thresh = 150;
    	Imgproc.Canny(map, edges, lo_thresh, hi_thresh);
    	
    	
    	double dp = 1;
    	double minDist = 1;
    	
    	Mat circles = new Mat();
    	Imgproc.HoughCircles(edges, circles, Imgproc.CV_HOUGH_GRADIENT, dp, minDist);
    	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
    	
    	double maxArea = 40;
    	float[] radius = new float[1];
    	org.opencv.core.Point center = new org.opencv.core.Point();
    	for (MatOfPoint c : contours) {
    		if (Imgproc.contourArea(c) > maxArea) {
                MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
                Imgproc.minEnclosingCircle(c2f, center, radius);
    		}
    	}
        Imgproc.circle(map, center, (int)radius[0], new Scalar(255, 0, 0), 2);
    	
    	return map;
    }
}
