package blackboard;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import mapping.LidarScan;
import objects.LidarSample;
import objects.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.opencv.core.CvType;
import org.opencv.core.Rect;

public class BLBallDetector {

	public static void main(String args[])
	{
		try {
			BLBallDetector bd = new BLBallDetector();

			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			FileInputStream fis = new FileInputStream("testScan4.data");
			ObjectInputStream ois = new ObjectInputStream(fis);

			LidarScan scan = (LidarScan) ois.readObject();
			ois.close();

			// graphheight in getGraph sometimes crashes if too low for data.
			//Mat graph = bd.getGraph(scan, 1);
			//Imgcodecs.imwrite("graph.png", graph);
			
			Mat map = bd.getMap(scan);
			Imgcodecs.imwrite("map.png", map);

			Point closestball = bd.findClosestBallLidar(scan);
			if (closestball == null) {
				System.out.println("No balls found.");
			} else {
				System.out.println("Closest ball found at "+closestball.toString());
			}

			//System.out.println(bd.scanCameraImage(CAMERA PHOTO));

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

	public Mat getMap(LidarScan scan) {
		int lx = 0;
		int hx = 0;
		int ly = 0;
		int hy = 0;
		
		List<Point> pts = scan.getPoints();
		
		for (Point p : pts) {
			if (p.x < lx)
				lx = p.x;
			
			if (p.x > hx)
				hx = p.x;
			
			if (p.y < ly)
				ly = p.y;
			
			if (p.y > hy)
				hy = p.y;
		}
		
		int w = Integer.max(Math.abs(lx), Math.abs(hx));
		int h = Integer.max(Math.abs(ly), Math.abs(hy));
		
		Mat mat = Mat.zeros(h*2+1, w*2+1, CvType.CV_8U);
		
		
		int lineThickness = 2;
		if (pts.size() >= 1) {
			Point prevPoint = pts.get(0);
			mat.put(h-prevPoint.y,  w+prevPoint.x, new byte[] {(byte)255});
			
			for (int i = 1; i < pts.size(); i++) {
				Point p = pts.get(i);
				mat.put(h-p.y, w+p.x, new byte[] {(byte)255});
				
				if (p.distance(prevPoint) <= 20) {
					Imgproc.line(mat, new org.opencv.core.Point(w+p.x, h-p.y), new org.opencv.core.Point(w+prevPoint.x, h-prevPoint.y), new Scalar(255), lineThickness, Imgproc.LINE_8, 0);
				}
				
				prevPoint = p;
			}
		}
		
		// (0, 0) cross which points at 0 degrees (to the right).
		//Imgproc.line(mat, new org.opencv.core.Point(w-10, h), new org.opencv.core.Point(w+30, h), new Scalar(127), 3, Imgproc.LINE_AA, 0);
		//Imgproc.line(mat, new org.opencv.core.Point(w, h-10), new org.opencv.core.Point(w, h+10), new Scalar(127), 3, Imgproc.LINE_AA, 0);
		
		return mat;
	}
	
	/**
	 * Draws a 2d graph of a lidar scan, x is the angle and y is the distance
	 * @param scan
	 * @param pixelDistPerDeg the horizontal resolution of the image
	 * @return the graph
	 */
	public Mat getGraph(LidarScan scan, double pixelDistPerDeg) {
		int graphheight = 10000;
		Mat graph = Mat.zeros(graphheight, (int) (360*pixelDistPerDeg), CvType.CV_8U);
		
		for (LidarSample ls : scan.getSamples())
		{
			graph.put(graphheight - (int) ls.distance, (int) (ls.angle*pixelDistPerDeg), new byte[] {(byte) 255});
		}
		
		return graph;
	}
	
	/**
	 * Looks for balls in a lidar scan
	 * @param scan
	 * @return the location of the closest ball relative to the lidar
	 */
	public Point findClosestBallLidar(LidarScan scan) {
		try {
			Mat map = getMap(scan);
			
			// Convert to binary image
			int thresh = 200;
			Mat map_bin = new Mat();
			Imgproc.threshold(map, map_bin, thresh, 255, Imgproc.THRESH_BINARY);
			
			// Dialate to connect points
			int dialation_value = 0;
			Mat dialation_kernel = Mat.ones(dialation_value, dialation_value, CvType.CV_8U);
			Mat map_dial = new Mat();
			Imgproc.dilate(map_bin, map_dial, dialation_kernel);
			
			// Find circles
			double dp = 1;
			double minDist = 35;
			int circleCurveParam1 = 500;
			int centerDetectionParam2 = 8;
			int minRadius = 10;
			int maxRadius = 45;
			Mat circles = new Mat();
			Imgproc.HoughCircles(map_dial, circles, Imgproc.HOUGH_GRADIENT, dp, minDist, circleCurveParam1, centerDetectionParam2, minRadius, maxRadius);
			
			// Find circle locations on image
			List<Point> ps = new ArrayList<Point>();
			for (int i = 0; i < circles.cols(); i++) {
				double[] c = circles.get(0, i);
				ps.add(new Point((int) c[0], (int) c[1]));
			}
			
			int centerW = (int) (map.size().width / 2);
			int centerH = (int) (map.size().height / 2);
			
			// Find closest circle to robot
			Point origo = new Point(centerW+1, centerH+1);
			Point closest = null;
			double closestdistance = Double.POSITIVE_INFINITY;
			for (Point p : ps) {
				if (origo.distance(p) < closestdistance) {
					closest = p;
					closestdistance = origo.distance(p);
				}
			}
			
			if (closest != null) {
				// Convert image location to physical location
				return new Point(closest.x-centerW, closest.y-centerH);
			}
			
			return null;
		}
		catch (Exception e) {
			// Sometimes the search fails for no clear reason
			// As long as it works most of the time, the scan
			// can just be repeatedly tried.
			return null;
		}
	}
	
	/**
	 * Scans a picture for orange balls
	 * @param frame picture to search in
	 * @return whether there is a ball in the picture
	 */
	public boolean scanCameraImage(Mat frame) {	
		int erosion_value = 0; 
		int dialation_value = 0;
		
		// Convert image to HSV format
		Mat hsv = new Mat();
		Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
		
		// Look for orange colors
		Scalar lower = new Scalar(0,0,0);
		Scalar upper = new Scalar(150,150,150);
		Mat orangemask = new Mat();
		Core.inRange(hsv, lower, upper, orangemask);
		Mat orange_output = new Mat();
		Core.bitwise_and(frame, frame, orange_output, orangemask);
		
		// Erode to get rid of noise
		Mat kernel = Mat.ones(erosion_value, erosion_value, CvType.CV_8U);
		Mat erosion = new Mat();
		Imgproc.erode(orangemask, erosion, kernel);

		// Dilate to undo erosion
		Mat kernel_dialation = Mat.ones(dialation_value, dialation_value, CvType.CV_8U);
		Mat dialationNerosion = new Mat();
		Imgproc.dilate(erosion, dialationNerosion, kernel_dialation);

		// Crop to region of interest
		Rect roi = new Rect(280, 10, 50, 440);
		Mat ball_roi = new Mat(dialationNerosion, roi);
		
		// Adaptive threshold image into two colors
		Mat thresh = new Mat();
		Imgproc.adaptiveThreshold(ball_roi, thresh, 127, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 40);
		
		// Find contours (balls are contours)
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

		return contours.size() != 0;
	}

	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}
}
