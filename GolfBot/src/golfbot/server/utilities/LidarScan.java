package golfbot.server.utilities;

import java.util.ArrayList;
import java.util.List;

import lejos.robotics.geometry.Point;

public class LidarScan {
	
	private List<LidarSample> samples;
	
	public LidarScan() {
		this.samples = new ArrayList<LidarSample>();
	}
	
	public LidarScan(List<LidarSample> scan) {
		this.samples = scan;
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
		Point pos = new Point(0,0);
		List<Point> mapping = new ArrayList<Point>();
		for(LidarSample sample : samples) {
			mapping.add(pos.pointAt((float)sample.distance, (float)sample.angle));
		}
		return mapping;
	}

	public int scanSize() {
		return this.samples.size();
	}
	
}
