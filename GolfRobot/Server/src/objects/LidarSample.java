package objects;

public class LidarSample {
	public double angle;
	public double distance;
	
	public LidarSample(double angle, double distance) {
		this.angle = angle;
		this.distance = distance;
	}
	
	public Point getRectangularCoordinates() {
		int x = (int) (distance * Math.cos(Math.toRadians(angle)));
		int y = (int) (distance * Math.sin(Math.toRadians(angle)));
		return new Point(x, y);
	}
}
