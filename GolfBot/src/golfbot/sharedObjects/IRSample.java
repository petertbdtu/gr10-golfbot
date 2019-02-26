package golfbot.sharedObjects;

public class IRSample {
	
	public float distance;
	
	public IRSample (float distance) {
		this.distance = distance;
	}
	
	public boolean equals(IRSample irSample) {
		return (this.distance == irSample.distance);
	}
	
	public String toString() {
		return "[" + distance + "]";
	}

}
