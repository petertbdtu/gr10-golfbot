package mapping;

import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
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
	
	public static Mat findLines(Mat mat) {
		Mat lines = new Mat();
		Mat matLines = new Mat();
		int rho = 1;
		double theta = Math.PI / 180;
		int threshold = 5;
		int min_line_length = 14;
		int max_line_gap = 30;		
		Imgproc.HoughLinesP(mat, lines, rho, theta, threshold, min_line_length, max_line_gap);
		//Drawing lines on the image
		System.out.println("Lines found: " + lines.cols());
        for (int i = 0; i < lines.cols(); i++) {
            double[] points = lines.get(0, i);
            double x1, y1, x2, y2;

            x1 = points[0];
            y1 = points[1];
            x2 = points[2];
            y2 = points[3];

            Point pt1 = new Point(x1, y1);
            Point pt2 = new Point(x2, y2);

            //Drawing lines on an image
            Imgproc.line(mat, pt1, pt2, new Scalar(255, 0, 0), 4);
        }
		return mat;
	}
	
	public static byte[] matToImageBuffer(Mat mat) {
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", mat, byteMat);
		return byteMat.toArray();
	}
	
	public static byte[] getAsImage(LidarScan scan) {
		Mat map = scanToMap(scan);
		map = drawCirclesOnMap(map, findAllBallsLidar(map));
		return matToImageBuffer(map);
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
	
	
	/*
	img = cv2.imread("TestScan.png")
	img_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	img_bw = cv2.threshold(img_gray, 200, 255, cv2.THRESH_BINARY)[1]
	line_image = np.copy(img) * 0  # creating a blank to draw lines on
	
	# Get lines
	rho = 1  # distance resolution in pixels of the Hough grid
	theta = np.pi / 180  # angular resolution in radians of the Hough grid
	threshold = 15  # minimum number of votes (intersections in Hough grid cell)
	min_line_length = 30  # minimum number of pixels making up a line
	max_line_gap = 100  # maximum gap in pixels between connectable line segments
	img_lines = cv2.HoughLinesP(img_bw, rho, theta, threshold, np.array([]), min_line_length, max_line_gap)
	for line in img_lines:
	    for x1,y1,x2,y2 in line:
	        print(line_angle(x1, y1, x2, y2))
	        ang = (y2-y1)/(x2-x1)
	        b = y1-ang*x1
	        
	        y_start = int(ang*0 + b)
	        y_end = int(ang*4000 + b)
	        
	        cv2.line(img_bw,(0,y_start),(4000,y_end),(0,0,0),20)
			cv2.line(line_image,(0,y_start),(4000,y_end),(0,0,0),20)
			
	return img_bw, line_image
	*/

}
