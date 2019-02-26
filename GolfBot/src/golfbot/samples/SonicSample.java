package golfbot.samples;

public class SonicSample {

	public float distance;
	
	public SonicSample (float distance) {
		this.distance = distance;
	}
	
	public boolean equals(SonicSample sonicSample) {
		return (this.distance == sonicSample.distance);
	}
	
	public String toString() {
		return "[" + distance + "]";
	}
	
}
