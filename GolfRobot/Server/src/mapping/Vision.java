package mapping;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;


//import objects.Point;
public class Vision {

	private static final int SQ_SIZE = 4000;
	private static final int CENTER_X = SQ_SIZE / 2;
	private static final int CENTER_Y = SQ_SIZE / 2;
	
	public static Mat readImageFromFile(String filePath) {
		Mat mat = Imgcodecs.imread(filePath, Imgcodecs.IMREAD_GRAYSCALE);
		Imgproc.threshold(mat, mat, 200, 255, Imgproc.THRESH_BINARY);
		return mat;
	}
	
	public static byte[] matToImageBuffer(Mat mat) {
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", mat, byteMat);
		byte[] res = byteMat.toArray();
		byteMat.release();
		return res;
	}
	
	public static byte[] getAsImage(LidarScan scan) {
		Mat map = scanToPointMap(scan);
		drawCirclesOnMap(map, findAllBallsLidar(map));
		return matToImageBuffer(map);
	}
	
	public static Mat findAllBallsLidar(Mat map) {
		// Convert to binary image
		int thresh = 200;
		Imgproc.threshold(map, map, thresh, 255, Imgproc.THRESH_BINARY);
		
		// Dialate to connect points
		int dialation_value = 0;
		Mat dialation_kernel = Mat.ones(dialation_value, dialation_value, CvType.CV_8U);
		Imgproc.dilate(map, map, dialation_kernel);
		
		// Find circles
		double dp = 1;
		double minDist = 45;
		int circleCurveParam1 = 1200;
		int centerDetectionParam2 = 8;
		int minRadius = 15;
		int maxRadius = 35;
		Mat circles = new Mat();
		Imgproc.HoughCircles(map, circles, Imgproc.HOUGH_GRADIENT, dp, minDist, circleCurveParam1, centerDetectionParam2, minRadius, maxRadius);
		return circles;
	}
	
	public static Mat findCirclesThatAreNotCircles(Mat map) {
		// Convert to binary image
		int thresh = 200;
		Imgproc.threshold(map, map, thresh, 255, Imgproc.THRESH_BINARY);
		
		// Dialate to connect points
		int dialation_value = 0;
		Mat dialation_kernel = Mat.ones(dialation_value, dialation_value, CvType.CV_8U);
		Imgproc.dilate(map, map, dialation_kernel);
		
		// Find circles
		double dp = 1;
		double minDist = 45;
		int circleCurveParam1 = 1200;
		int centerDetectionParam2 = 8;
		int minRadius = 15;
		int maxRadius = 35;
		Mat circles = new Mat();
		Imgproc.HoughCircles(map, circles, Imgproc.HOUGH_GRADIENT, dp, minDist, circleCurveParam1, centerDetectionParam2, minRadius, maxRadius);
		return circles;
	}
	
	public static List<objects.Point> getCircleLocsFromMat(Mat circles) {
		List<objects.Point> ps = new ArrayList<objects.Point>();
		for (int i = 0; i < circles.cols(); i++) {
			double[] c = circles.get(0, i);
			ps.add(new objects.Point((int) c[0] - CENTER_X, (int) c[1] - CENTER_Y));
		}
		return ps;
	}
	
	public static Mat scanToPointMap(LidarScan scan) {
		List<objects.Point> pts = scan.getPoints();
		
		Mat mat = Mat.zeros(SQ_SIZE, SQ_SIZE, CvType.CV_8U);
		
		for (int i = 0; i < pts.size(); i++) {
			objects.Point p = pts.get(i);
			int pxl_x = CENTER_X - p.x;
			int pxl_y = CENTER_Y - p.y;
			if (0 <= pxl_x && pxl_x <= SQ_SIZE && 0 <= pxl_y && pxl_y <= SQ_SIZE) {
				mat.put(pxl_y, pxl_x, new byte[] {(byte)255});
			}
		}
		
		return mat;
	}
	
	
	public static Mat scanToLineMap(LidarScan scan) {
		List<objects.Point> pts = scan.getPoints();
		Mat mat = Mat.zeros(SQ_SIZE, SQ_SIZE, CvType.CV_8U);

		if (pts.size() >= 1) {
			objects.Point prevPoint = pts.get(0);
			for (int i = 1; i < pts.size(); i++) {
				objects.Point curPoint = pts.get(i);
				int pxl_x = CENTER_X - curPoint.x;
				int pxl_y = CENTER_Y - curPoint.y;

				// Draw line between points
				if (curPoint.distance(prevPoint) <= 20) {
					Imgproc.line(mat,
							new Point(CENTER_X - prevPoint.x, CENTER_Y - prevPoint.y),
							new Point(pxl_x, pxl_y),
							new Scalar(255), 3);
				}
				
				prevPoint = curPoint;
			}
		}		
		return mat;
	}
	
	public static void drawRobotMarker(Mat map) {
		Imgproc.line(map, new Point(CENTER_X-10, CENTER_Y), new Point(CENTER_X+30, CENTER_Y), new Scalar(127), 3, Imgproc.LINE_AA, 0);
		Imgproc.line(map, new Point(CENTER_X, CENTER_Y-10), new Point(CENTER_X, CENTER_Y+10), new Scalar(127), 3, Imgproc.LINE_AA, 0);
	}
	
	public static void drawCirclesOnMap(Mat map, Mat circles) {
		for (int i = 0; i < circles.cols(); i++) {
			double[] c = circles.get(0, i);
			if(c.length > 1) {
	            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
	            int radius = (int) Math.round(c[2]);
	            Imgproc.circle(map, center, radius, new Scalar(255,255,0), -1, 8, 0 );
			}
		}
	}
	
	public static void removeCirclesFromMat(Mat map, Mat circles) {
		for (int i = 0; i < circles.cols(); i++) {
			double[] c = circles.get(0, i);
			if(c.length > 1) {
	            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
	            int radius = (int) Math.round(c[2]);
	            Imgproc.circle(map, center, radius, new Scalar(0,0,0), -1, 8, 0 );
			}
		}
	}
	
	/**
	 * Finds walls / lines and returns a mask of their position
	 * @param map Map to search in
	 * @return Mask of walls
	 */
	public static Mat findWalls(Mat map) {
		Mat walls = new Mat(map.size(), map.type());
		
		Mat lines = findLines(map);
		for (int i = 0; i < lines.rows(); i++) {
			double[] l = lines.get(i, 0);
			double x1 = l[0];
			double y1 = l[1];
			double x2 = l[2];
			double y2 = l[3];
			Point pt1 = new Point(x1, y1);
			Point pt2 = new Point(x2, y2);
			Imgproc.line(walls, pt1, pt2, new Scalar(255,255,255), 7);
		}
		lines.release();
		
		lines = findWallLines(walls);
		for (int i = 0; i < lines.rows(); i++) {
			double[] l = lines.get(i, 0);
			double x1 = l[0];
			double y1 = l[1];
			double x2 = l[2];
			double y2 = l[3];
			
			double a = (y2-y1) / (x2-x1);
			double b = y1 - a*x1;
			double y_start = (b); // a*0 + b
			double y_end = (a*SQ_SIZE + b);

			Point pt1 = new Point(0, y_start);
			Point pt2 = new Point(SQ_SIZE, y_end);
			Imgproc.line(walls, pt1, pt2, new Scalar(255,255,255), 7);
		}
		lines.release();
		return walls;
	}
	
	public static void drawMoreLinesOnMap(Mat map) {
		int rho = 1;
		double theta = Math.PI / 180;
		int threshold = 2;
		int min_line_length = 2;
		int max_line_gap = 10;	
		
		Mat lines = new Mat();
		Imgproc.HoughLinesP(map, lines, rho, theta, threshold, min_line_length, max_line_gap);
		for (int i = 0; i < lines.rows(); i++) {
			double[] l = lines.get(i, 0);
			double x1 = l[0];
			double y1 = l[1];
			double x2 = l[2];
			double y2 = l[3];
			Point pt1 = new Point(x1, y1);
			Point pt2 = new Point(x2, y2);
			Imgproc.line(map, pt1, pt2, new Scalar(255,255,255), 2);
		}
		lines.release();
	}
	
	public static Mat findLines(Mat map) {
		int rho = 1;
		double theta = Math.PI / 180;
		int threshold = 42;
		int min_line_length = 36;
		int max_line_gap = 60;		
		Mat lines = new Mat();
		Imgproc.HoughLinesP(map, lines, rho, theta, threshold, min_line_length, max_line_gap);
		return lines;
	}
	
	public static Mat findWallLines(Mat map) {
		int rho = 1;
		double theta = Math.PI / 180;
		int threshold = 150;
		int min_line_length = 220;
		int max_line_gap = 100;		
		Mat lines = new Mat();
		Imgproc.HoughLinesP(map, lines, rho, theta, threshold, min_line_length, max_line_gap);
		return lines;
	}
	
	public static List<objects.Point> collisionMapToPoints(Mat map) {
		List<objects.Point> points = new ArrayList<objects.Point>();
		for (int x = 0; x < SQ_SIZE; x++) {
			for (int y = 0; y < SQ_SIZE; y++) {
				if (map.get(x,y)[0] == 255) {
					points.add(new objects.Point(x, y));
				}
			}
		}
		return points;
	}
	
	public static void saveScan(LidarScan scan) {
		Imgcodecs.imwrite("help.jpg", scanToPointMap(scan));
	}
	
	/**
	 * Scans a picture for orange balls
	 * @param image picture to search in
	 * @return whether there is a ball in the picture
	 */
	public static boolean detectBallInImage(byte[] image) {
		Mat frame = Imgcodecs.imdecode(new MatOfByte(image), Imgcodecs.IMREAD_COLOR);
		
		int erosion_value = 0; 
		int dialation_value = 0;
		
		// Convert image to HSV format
		Mat hsv = new Mat();
		Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
		
		// Look for orange colors
		Scalar lower = new Scalar(10,140,120);
		Scalar upper = new Scalar(60,220,200);
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
	public static objects.Point findGoal(Mat map, Mat print) {		
		// ONLY LOOKS TO THE RIGHT OF THE ROBOT, ADJUST!
		Mat roi = map.clone();
		Point a = new Point((SQ_SIZE/2)-300, (SQ_SIZE/2)-700);
		Point b = new Point((SQ_SIZE/2)+200, (SQ_SIZE/2)+50);
		Imgproc.rectangle(print,a,b,new Scalar(255,255,0),10);
		roiWithoutCrop(roi, a, b);

		// Dialate
		int dialation_value = 0;
		Mat dialation_kernel = Mat.ones(dialation_value, dialation_value, CvType.CV_8U);
		Imgproc.dilate(roi, roi, dialation_kernel);
		
		// HoughLines
		int rho = 1;
		double theta = Math.PI / 180;
		int threshold = 2;
		int min_line_length = 2;
		int max_line_gap = 70;		
		
		// ALSO WRITES TO linesOUT
		Mat lines = new Mat();
		Imgproc.HoughLinesP(roi, lines, rho, theta, threshold, min_line_length, max_line_gap);
		for (int i = 0; i < lines.rows(); i++) {
			double[] l = lines.get(i, 0);
			double x1 = l[0];
			double y1 = l[1];
			double x2 = l[2];
			double y2 = l[3];
			Point pt1 = new Point(x1, y1);
			Point pt2 = new Point(x2, y2);
			Imgproc.line(roi, pt1, pt2, new Scalar(255,255,255), 5);
		}
				
		// Gaussian Blur
		Imgproc.GaussianBlur(roi, roi, new Size(21,21), 0);
		
		// Detect Corners
		MatOfPoint corners = new MatOfPoint();
		Imgproc.goodFeaturesToTrack(roi, corners, 4, 0.50, 45.0);
		List<Point> cornerList = corners.toList();
		
		// Draw corners (ostensibly goals)
		for (Point c : cornerList) {
			Imgproc.circle(print, c, 20, new Scalar(0,255,255), -1);
		}
		corners.release();
		
		if (cornerList.size() == 4) {
			cornerList.sort( (Point p1, Point p2) -> {
				if (p1.x < p2.x)
					return -1;
				if (p1.x > p2.x)
					return 1;
				return 0;
			});
			
			// Take middle two points (goal edges) and take average (goal center)
			Point c1 = cornerList.get(1);
			Point c2 = cornerList.get(2);
			Point avg = new Point((c1.x+c2.x)/2, (c1.y+c2.y)/2);
			objects.Point goal = new objects.Point((int)(avg.x-CENTER_X), (int)(avg.y-CENTER_Y));
			return goal;
		}
		
		return null;
	}

	public static void roiWithoutCrop(Mat map, Point a, Point b) {
		Mat mask = Mat.zeros(SQ_SIZE, SQ_SIZE, CvType.CV_8U);
		Imgproc.rectangle(mask, a, b, new Scalar(255), -1, 8, 0);
		Core.bitwise_and(map, mask, map);
	}
	
	public static void drawGoalPoint(Mat map, objects.Point goal) {
		Point g = new Point(goal.x+CENTER_X, goal.y+CENTER_Y);
        Imgproc.circle(map, g, 40, new Scalar(0,255,0), -1);
	}
}
