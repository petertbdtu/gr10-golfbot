package org.Server;

public class Blackboard {	
	
	private boolean bbUpdated = false;
	public BlackboardSample bbSample;
	private static Blackboard bb;
	private long cycle = 0;

	public static Blackboard getInstance() {
		if(bb == null) { bb = new Blackboard(); }
		return bb;
	}
	
	public boolean isUpdated() {
		return bbUpdated;
	}
	
	public void incrementCycle() {
		cycle++;
		bbUpdated = false;
	}
	
	public void setBlackboardSample(BlackboardSample bbSample) {
		this.bbSample = bbSample;
		this.bbSample.cycle = this.cycle;
		bbUpdated = true;
	}
	
	public BlackboardSample getBlackboardSample() {
		return bbSample;
	}
}
