

import java.io.Serializable;

public class PoseSample implements Serializable{

	private static final long serialVersionUID = 1L;
	public float x;
	public float y;
	public float heading;
	
	public PoseSample(float x, float y, float heading) {
		this.x = x;
		this.y = y;
		this.heading = heading;
	}
	
	public boolean equals(PoseSample poseSample) {
		return (this.x == poseSample.x && this.y == poseSample.y && this.heading == poseSample.heading);
	}
	
	public String toString() {
		return "[" + this.x + "," + this.y + "," + this.heading + "]";
	}
}
