package golfbot.server.blackboard;

import java.util.ArrayList;

public class BlackboardController extends Thread {
	private ArrayList<BlackboardListener> bbListeners;
	private BlackboardSample bSample;
	private long cycle = 0;
	
	public void registerListener(BlackboardListener bbListener) {
		bbListeners.add(bbListener);
	}
	
	@Override
	public void run() {
		while(!bbListeners.isEmpty()) {
			if(newBlackboardValues()) {
				notifyListeners(bSample);
			}
		}
	}
	
	private void notifyListeners(BlackboardSample bs) {
		for (BlackboardListener bl : bbListeners) {
			bl.blackboardUpdated(bs);
		}
	}
	
	private boolean newBlackboardValues() {
		return false;
	}
}
