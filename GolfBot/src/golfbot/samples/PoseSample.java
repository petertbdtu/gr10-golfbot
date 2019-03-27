package golfbot.samples;

import lejos.robotics.navigation.Pose;

public class PoseSample {

	public Pose position;
	
	public PoseSample(Pose position) {
		this.position = position;
	}
	
	public boolean equals(PoseSample poseSample) {
		return (this.position == poseSample.position);
	}
	
	public String toString() {
		return "[" + position + "]";
	}
}
