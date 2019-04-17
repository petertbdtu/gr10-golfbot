package golfbot.samples;

public class NavigationSample {
	public boolean isDriving;

	public NavigationSample(boolean isDriving) {
		this.isDriving = isDriving;
	}
	
	public boolean equals(NavigationSample sample) {
		return (this.isDriving == sample.isDriving);
	}

	public String toString() {
		return "[" + isDriving + "]";
	}	
}
