package golfbot.server.blackboard;

import java.util.ArrayList;

public class BlackboardController extends Thread {
	private ArrayList<BlackboardListener> bbListeners;
	
	public void registerListener(BlackboardListener bbListener) {
		bbListeners.add(bbListener);
	}
	
	@Override
	public void run() {
		Blackboard bb = Blackboard.getInstance();
		while(!bbListeners.isEmpty()) {
			if(bb.isUpdated()) {
				BlackboardSample bbSample = bb.getBlackboardSample();
				notifyListeners(bbSample);
				bb.incrementCycle();
			}
		}
	}
	
	private void notifyListeners(BlackboardSample bs) {
		for (BlackboardListener bl : bbListeners) {
			bl.blackboardUpdated(bs);
		}
	}
}
