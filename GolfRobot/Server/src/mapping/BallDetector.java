package mapping;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import blackboard.BlackboardListener;
import blackboard.BlackboardSample;
import objects.LidarSample;
import objects.Point;

public class BallDetector {
	
	private final int SQ_SIZE = 4000;
	private final int CENTER_X = SQ_SIZE / 2;
	private final int CENTER_Y = SQ_SIZE / 2;
	private final Point CENTER = new Point(CENTER_X, CENTER_Y);
	
	private volatile Point closestBall;
	private volatile Mat markedMap;
	private long counter = 0;
	
	public Point getClosestBall() {
		if(closestBall != null) {
			Imgcodecs.imwrite("Scanning" + counter++ + ".png", markedMap);
			System.out.println("Ny Scanning Gemt");
		}
		return closestBall;
	}
	
	public Mat getMarkedMap() {
		return markedMap;
	}

	/**
	 * Looks for balls in a specific direction in a lidar scan
	 * @param scan
	 * @param lower lower bound angle on the direction, in degrees
	 * @param upper upper bound angle on the direction, in degrees
	 * @return the location of the closest ball relative to the lidar, in the region
	 */
	public Point findClosestBallInDirection(LidarScan scan, float lower, float upper) {
		LidarScan directionalScan = new LidarScan();
		
		for (LidarSample sample : scan.getSamples()) {
			if (lower < sample.angle && sample.angle < upper) {
				directionalScan.addSample(sample);
			}
		}
		
		return findClosestBallLidar(directionalScan);
	}
	
	/**
	 * Looks for balls in a lidar scan
	 * @param scan
	 * @return the location of the closest ball relative to the lidar
	 */
	public Point findClosestBallLidar(LidarScan scan) {
		try {
			Mat map = scanToMap(scan);
			Mat circles = findAllBallsLidar(map);
			List<Point> ps = getCircleLocsFromMat(circles);
			if (ps.size() > 0) {
				Point origo = getImageCenterPoint();
				Point closest = findClosestPointToPoint(ps, origo);
				
				markedMap = drawCirclesOnMap(map, circles);
				//Imgcodecs.imwrite("Scanning" + counter++ + ".png", markedMap);

				return subtractPoints(closest, origo);
			}
			return null;
			}
		catch (Exception e) {
			// Only fails sometimes, can just be repeatedly tried.
			return null;
		}
	}
	
	public Point subtractPoints(Point p, Point d) {
			return new Point(p.x-d.x, p.y-d.y);
	}

	public Point getImageCenterPoint() {
		return new Point(CENTER_X, CENTER_Y);
	}
	
	/**
	 * Convert a LidarScan to an image which is centered around the lidar.
	 */
	public Mat scanToMap(LidarScan scan) {
		List<Point> pts = scan.getPoints();
		
		Mat mat = Mat.zeros(SQ_SIZE, SQ_SIZE, CvType.CV_8U);
		
		if (pts.size() >= 1) {
			Point prevPoint = pts.get(0);
			
			for (int i = 0; i < pts.size(); i++) {
				Point curPoint = pts.get(i);
				int pxl_x = CENTER_X - curPoint.x;
				int pxl_y = CENTER_Y - curPoint.y;
				
				// Draw point
				//if (0 <= pxl_x && pxl_x <= SQ_SIZE && 0 <= pxl_y && pxl_y <= SQ_SIZE) {
				//	mat.put(pxl_y, pxl_x, new byte[] {(byte)255});
				//}
				
				// Draw line between points
				if (curPoint.distance(prevPoint) <= 20) {
					Imgproc.line(mat,
							new org.opencv.core.Point(CENTER_X - prevPoint.x, CENTER_Y - prevPoint.y),
							new org.opencv.core.Point(pxl_x, pxl_y),
							new org.opencv.core.Scalar(255), 3);
				}
				
				prevPoint = curPoint;
			}
		}
		
		
		// (0, 0) cross which points at 0 degrees.
		Imgproc.line(mat, new org.opencv.core.Point(CENTER_X-10, CENTER_Y), new org.opencv.core.Point(CENTER_X+30, CENTER_Y), new Scalar(127), 3, Imgproc.LINE_AA, 0);
		Imgproc.line(mat, new org.opencv.core.Point(CENTER_X, CENTER_Y-10), new org.opencv.core.Point(CENTER_X, CENTER_Y+10), new Scalar(127), 3, Imgproc.LINE_AA, 0);
		
		return mat;
	}

	public Point findClosestPointToPoint(List<Point> points, Point origo) {
		Point closest = null;
		double closestdistance = Double.POSITIVE_INFINITY;
		for (Point p : points) {
			if (origo.distance(p) < closestdistance) {
				closest = p;
				closestdistance = origo.distance(p);
			}
		}
		return closest;
	}

	public List<Point> getCircleLocsFromMat(Mat circles) {
		List<Point> ps = new ArrayList<Point>();
		for (int i = 0; i < circles.cols(); i++) {
			double[] c = circles.get(0, i);
			ps.add(new Point((int) c[0], (int) c[1]));
		}
		return ps;
	}
	
	public Mat findAllBallsLidar(Mat map) {
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
		int circleCurveParam1 = 300;
		int centerDetectionParam2 = 9;
		int minRadius = 15;
		int maxRadius = 25;
		Mat circles = new Mat();
		Imgproc.HoughCircles(map_dial, circles, Imgproc.HOUGH_GRADIENT, dp, minDist, circleCurveParam1, centerDetectionParam2, minRadius, maxRadius);
		
		return circles;
	}
	
	public Mat drawCirclesOnMap(Mat map, Mat circles) {
		Mat res = new Mat();
		Imgproc.cvtColor(map, res, Imgproc.COLOR_GRAY2BGR);
		
		for (int i = 0; i < circles.cols(); i++) {
			double[] c = circles.get(0, i);
            org.opencv.core.Point center = new org.opencv.core.Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(res, center, 1, new Scalar(0,100,100), 3, 8, 0 );
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(res, center, radius, new Scalar(255,0,255), 3, 8, 0 );
		}
		
		return res;
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

	public byte[] getByteArrayFromLidarScan(LidarScan scan) {
		Mat map = scanToMap(scan);
		markedMap = drawCirclesOnMap(map, findAllBallsLidar(map));
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", markedMap, byteMat);
		return byteMat.toArray();
	}
}
