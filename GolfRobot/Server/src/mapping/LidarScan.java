package mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import objects.LidarSample;
import objects.Point;

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
}
