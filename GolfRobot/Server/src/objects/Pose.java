package objects;

public class Pose {

	public Point point;
	public float heading;
	
	public Pose(int x, int y, float heading) {
		this.point = new Point(x,y);
		this.heading = heading;
	}
	
	public Pose() {
		this.point = new Point(0, 0);	
		this.heading = 0;
	}
	public Pose(Pose pose) {
		this.point.x = pose.point.x;
		this.point.y = pose.point.y;
		this.heading = pose.heading;
	}
	
	public boolean equals(Pose pose) {
		return (this.point.x == pose.point.x && this.point.y == pose.point.y && this.heading == pose.heading);
	}
	
	public String toString() {
		return "[" + this.point.x + "," + this.point.y + "," + this.heading + "]";
	}
}
