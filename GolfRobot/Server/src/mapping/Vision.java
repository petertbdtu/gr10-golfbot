package mapping;

import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import objects.Point;

public class Vision {

	private static final int SQ_SIZE = 4000;
	private static final int CENTER_X = SQ_SIZE / 2;
	private static final int CENTER_Y = SQ_SIZE / 2;
	private static final Point CENTER = new Point(CENTER_X, CENTER_Y);
	
	public static byte[] getAsImage(LidarScan scan) {
		Mat map = scanToMap(scan);
		map = drawCirclesOnMap(map, findAllBallsLidar(map));
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", map, byteMat);
		return byteMat.toArray();
	}
	
	public static Mat findAllBallsLidar(Mat map) {
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
	
	public static Mat scanToMap(LidarScan scan) {
		List<Point> pts = scan.getPoints();
		
		Mat mat = Mat.zeros(SQ_SIZE, SQ_SIZE, CvType.CV_8U);
		
		for (int i = 0; i < pts.size(); i++) {
			Point p = pts.get(i);
			int pxl_x = CENTER_X - p.x;
			int pxl_y = CENTER_Y - p.y;
			if (0 <= pxl_x && pxl_x <= SQ_SIZE && 0 <= pxl_y && pxl_y <= SQ_SIZE) {
				mat.put(pxl_y, pxl_x, new byte[] {(byte)255});
			}
		}
		
		// (0, 0) cross which points at 0 degrees.
		Imgproc.line(mat, new org.opencv.core.Point(CENTER_X-10, CENTER_Y), new org.opencv.core.Point(CENTER_X+30, CENTER_Y), new Scalar(127), 3, Imgproc.LINE_AA, 0);
		Imgproc.line(mat, new org.opencv.core.Point(CENTER_X, CENTER_Y-10), new org.opencv.core.Point(CENTER_X, CENTER_Y+10), new Scalar(127), 3, Imgproc.LINE_AA, 0);
		
		return mat;
	}
	
	public static Mat drawCirclesOnMap(Mat map, Mat circles) {
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

}
