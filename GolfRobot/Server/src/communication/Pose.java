package communication;

import java.io.Serializable;

public class Pose implements Serializable{

	private static final long serialVersionUID = 1L;
	public int x;
	public int y;
	public float heading;
	
	public Pose(int x, int y, float heading) {
		this.x = x;
		this.y = y;
		this.heading = heading;
	}
	
	public Pose() {
	}
	
	public boolean equals(Pose poseSample) {
		return (this.x == poseSample.x && this.y == poseSample.y && this.heading == poseSample.heading);
	}
	
	public String toString() {
		return "[" + this.x + "," + this.y + "," + this.heading + "]";
	}
}
