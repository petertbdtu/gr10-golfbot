package mapping;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

//import objects.Point;

public class Vision {

	private static final int SQ_SIZE = 4000;
	private static final int CENTER_X = SQ_SIZE / 2;
	private static final int CENTER_Y = SQ_SIZE / 2;
	private static final objects.Point CENTER = new objects.Point(CENTER_X, CENTER_Y);
	
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
		double minDist = 35;
		int circleCurveParam1 = 300;
		int centerDetectionParam2 = 8;
		int minRadius = 15;
		int maxRadius = 25;
		Mat circles = new Mat();
		Imgproc.HoughCircles(map, circles, Imgproc.HOUGH_GRADIENT, dp, minDist, circleCurveParam1, centerDetectionParam2, minRadius, maxRadius);
		map.release();
		return circles;
	}
	
	public static List<objects.Point> getCircleLocsFromMat(Mat circles) {
		List<objects.Point> ps = new ArrayList<objects.Point>();
		for (int i = 0; i < circles.cols(); i++) {
			double[] c = circles.get(0, i);
			ps.add(new objects.Point((int) c[0], (int) c[1]));
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
		for (int i = 0; i < circles.rows(); i++) {
			double[] c = circles.get(i, 0);
			if(c.length > 1) {
	            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
	            // circle center
	            Imgproc.circle(map, center, 1, new Scalar(0,100,100), 3, 8, 0 );
	            // circle outline
	            int radius = (int) Math.round(c[2]);
	            Imgproc.circle(map, center, radius, new Scalar(255,0,255), 3, 8, 0 );
			}
		}
		circles.release();
	}
	
	/**
	 * Finds lines (walls) and paints them black / erases them
	 * @param mapInOut map with lines to find and remove
	 * @param linesOut lines found
	 */
	public static void findWallsAndRemove(Mat mapInOut, Mat wallsOut) {
		Mat lines = findLines(mapInOut);
		for (int i = 0; i < lines.rows(); i++) {
			double[] l = lines.get(i, 0);
			double x1 = l[0];
			double y1 = l[1];
			double x2 = l[2];
			double y2 = l[3];
			Point pt1 = new Point(x1, y1);
			Point pt2 = new Point(x2, y2);
			Imgproc.line(mapInOut, pt1, pt2, new Scalar(0,0,0), 20);
			Imgproc.line(wallsOut, pt1, pt2, new Scalar(255,255,255), 20);
		}
		
		
		lines = findWallLines(wallsOut);
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
			
			Imgproc.line(mapInOut, pt1, pt2, new Scalar(0,0,0), 20);
			Imgproc.line(wallsOut, pt1, pt2, new Scalar(255,255,255), 2);
		}
		lines.release();
		
//		MatOfPoint corners = new MatOfPoint();
//		Imgproc.goodFeaturesToTrack(wallsOut, corners, 4, 0.5, 900.0);
//		for (int i = 0; i < corners.rows(); i++) {
//			double[] corner = corners.get(i,0);
//			System.out.println("ehhhh: " + corner[0] + ":" + corner[1]);
//			Imgproc.circle(wallsOut, new Point(corner[0],corner[1]), 100, new Scalar(255,255,255), -1);
//		}
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
		int threshold = 2;
		int min_line_length = 30;
		int max_line_gap = 10;		
		
		// ALSO WRITES TO linesOUT
		// img_lines = cv2.HoughLinesP(img_bw, rho, theta, threshold, np.array([]), min_line_length, max_line_gap)
		Mat lines = new Mat();
		Imgproc.HoughLinesP(map, lines, rho, theta, threshold, min_line_length, max_line_gap);
		
		return lines;
	}
	
	public static Mat findWallLines(Mat map) {
		int rho = 1;
		double theta = Math.PI / 180;
		int threshold = 150;
		int min_line_length = 190;
		int max_line_gap = 100;		
		
		// ALSO WRITES TO linesOUT
		// img_lines = cv2.HoughLinesP(img_bw, rho, theta, threshold, np.array([]), min_line_length, max_line_gap)
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
}
