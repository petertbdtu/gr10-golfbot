package mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import objects.LidarSample;
import objects.Point;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

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
	
	public Mat getMat() {
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
		
		int h, w;
		if (Math.abs(lx) > Math.abs(hx))
			w = lx;
		else
			w = hx;
		
		if (Math.abs(ly) > Math.abs(hy))
			h = ly;
		else
			h = hy;
		
		Mat mat = Mat.zeros(h*2+1, w*2+1, CvType.CV_8U);
		
		for (Point p : pts) {
			mat.put(p.y+h, p.x+w, new byte[] {(byte)255});
		}
		
		// (0, 0) location in grey
		mat.put(h, w, new byte[] {(byte)127});
		
		return mat;
	}
}
