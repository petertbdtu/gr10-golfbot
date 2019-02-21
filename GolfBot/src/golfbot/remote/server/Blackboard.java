package golfbot.remote.server;

public class Blackboard {	
	
	private static Blackboard bb;
	
	public static Blackboard getInstance() {
		if(bb == null) { bb = new Blackboard(); }
		return bb;
	}
	
}
