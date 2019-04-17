package golfbot.samples;

public class BallSample {
	public boolean isCollected;

	public BallSample(boolean isCollected) {
		this.isCollected = isCollected;
	}
	
	public boolean equals(BallSample sample) {
		return (this.isCollected == sample.isCollected);
	}

	public String toString() {
		return "[" + isCollected + "]";
	}	
}
