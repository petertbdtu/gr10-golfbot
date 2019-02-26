package golfbot.sharedObjects;

public class GyroSample {

	public float rate;
	public float angle;
	
	public GyroSample(float rate, float angle) {
		this.rate = rate;
		this.angle = angle;
	}
	
	public boolean equals(GyroSample gyroSample) {
		return (this.rate == gyroSample.rate && this.angle == gyroSample.angle);
	}
	
	public String toString() {
		return "[" + rate + ":" + angle + "]";
	}
	
}
