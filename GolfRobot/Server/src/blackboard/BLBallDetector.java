package blackboard;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import mapping.LidarScan;
import objects.LidarSample;
import objects.Point;

public class BLBallDetector extends Thread implements BlackboardListener {
	
	private BlackboardSample bbSample;
	private Point closestBall;
	
	public Point getClosestBall() {
		return closestBall;
	}
	
	@Override
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = bbSample;
	}

	@Override
	public void run() {		
		int cycle = -1;
		while(true) {
			if(bbSample != null) {
				if(bbSample.cycle == cycle + 1) {
					closestBall = findClosestBallLidar(bbSample.scan);
					cycle++;
				}
			}
		}
	}

	public Mat scanToMap(LidarScan scan) {
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

		// (0, 0) cross which points at 0 degrees.
		Imgproc.line(mat, new org.opencv.core.Point(w-10, h), new org.opencv.core.Point(w+30, h), new Scalar(127), 3, Imgproc.LINE_AA, 0);
		Imgproc.line(mat, new org.opencv.core.Point(w, h-10), new org.opencv.core.Point(w, h+10), new Scalar(127), 3, Imgproc.LINE_AA, 0);
		
		return mat;
	}
	
	/**
	 * Draws a 2d graph of a lidar scan, x is the angle and y is the distance
	 * @param scan
	 * @param pixelDistPerDeg the horizontal resolution of the image
	 * @return the graph
	 */
	@SuppressWarnings("unused")
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
			Mat map = scanToMap(scan);
			
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
			int circleCurveParam1 = 200;
			int centerDetectionParam2 = 9;
			int minRadius = 15;
			int maxRadius = 40;
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
	 * @param image picture to search in
	 * @return whether there is a ball in the picture
	 */
	public boolean detectBallInImage(BufferedImage image) {
		Mat frame = bufferedImageToMat(image);
		
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
