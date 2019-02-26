package golfbot.samples;

public class TouchSample {
	
	public float pressed;
	
	public TouchSample (float pressed) {
		this.pressed = pressed;
	}
	
	public boolean equals(TouchSample touchSample) {
		return (this.pressed == touchSample.pressed);
	}
	
	public String toString() {
		return "[" + pressed + "]";
	}

}
