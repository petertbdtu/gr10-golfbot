package golfbot.samples;

public class DrivingMotorSample {

	public int leftTachoCount;
	public int rightTachoCount;
	
	public DrivingMotorSample(int leftTachoCount, int rightTachoCount) {
		this.leftTachoCount = leftTachoCount;
		this.rightTachoCount = rightTachoCount;
	}
	
	public boolean equals(DrivingMotorSample motorSample) {
		return (this.leftTachoCount == motorSample.leftTachoCount && this.rightTachoCount == motorSample.rightTachoCount);
	}
	
	public String toString() {
		return "[" + leftTachoCount + ":" + rightTachoCount + "]";
	}
	
}
