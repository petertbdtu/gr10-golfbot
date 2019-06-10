package mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import objects.LidarSample;
import objects.Point;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class LidarScan implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private List<LidarSample> samples;
	
	public LidarScan() {
		this.samples = new ArrayList<LidarSample>();
	}
	
	public LidarScan(List<LidarSample> scan) {
		this.samples = new ArrayList<LidarSample>(scan);
	}
	
	public LidarScan(LidarScan scan) {
		this.samples = new ArrayList<LidarSample>(scan.samples);
	}
	
	public void addSample(LidarSample sample) {
		samples.add(sample);
	}
	
	public void addSample(double angle, double distance) {
		samples.add(new LidarSample(angle,distance));
	}
	
	public List<LidarSample> getSamples() {
		return samples;
	}

	public void clear() {
		samples.clear();
	}
	
	public List<Point> getPoints() {
		List<Point> mapping = new ArrayList<Point>();
		
		for (LidarSample sample : samples) {
			mapping.add(sample.getRectangularCoordinates());
		}
		
		return mapping;
	}

	public int scanSize() {
		return this.samples.size();
	}
	
	public Mat getMap() {
		int lx = 0;
		int hx = 0;
		int ly = 0;
		int hy = 0;
		
		List<Point> pts = getPoints();
		
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
		
		if (pts.size() >= 1) {
			Point prevPoint = pts.get(0);
			mat.put(h-prevPoint.x,  w-prevPoint.y, new byte[] {(byte)255});
			
			for (int i = 1; i < pts.size(); i++) {
				Point p = pts.get(i);
				mat.put(h-p.y, w-p.x, new byte[] {(byte)255});
				
				if (p.distance(prevPoint) <= 20) {
					Imgproc.line(mat, new org.opencv.core.Point(w-p.x, h-p.y), new org.opencv.core.Point(w-prevPoint.x, h-prevPoint.y), new Scalar(255), 1, Imgproc.LINE_AA, 0);
				}
				
				prevPoint = p;
			}
		}
		
		/*
		Mat lines = new Mat();
		double rho = 1.0; // distance resolution in pixels of the Hough grid
		double theta = Math.PI / 360; // angular resolution in radians of the Hough grid
		int threshold = 1; // minimum number of votes (intersections in Hough grid cell)
		int minLinLength = 2;
		int maxLineGap = 30;
		Imgproc.HoughLinesP(mat, lines, rho, theta, threshold, minLinLength, maxLineGap);
		
		for (int i = 0; i < lines.rows(); i++) {
			double[] l = lines.get(i, 0);
			Imgproc.line(mat, new org.opencv.core.Point(l[0], l[1]), new org.opencv.core.Point(l[2], l[3]), new Scalar(255), 3, Imgproc.LINE_AA, 0);
		}
		
		*/
		
		// (0, 0) location in grey
		//mat.put(h, w, new byte[] {(byte)127});
		
		return mat;
	}
	
	public Mat getGraph(double pixelDistPerDeg) {
		int graphheight = 2000;
		Mat graph = Mat.zeros(graphheight, (int) (360*pixelDistPerDeg), CvType.CV_8U);
		
		for (LidarSample ls : samples)
		{
			graph.put(graphheight - (int) ls.distance, (int) (ls.angle*pixelDistPerDeg), new byte[] {(byte) 255});
		}
		
		return graph;
	}
}
