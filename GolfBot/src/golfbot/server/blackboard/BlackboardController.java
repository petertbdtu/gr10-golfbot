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
	
	private void notifyListeners(BlackboardSample bbSample) {
		for (int i = 0 ; i < bbListeners.size() ; i++) {
			bbListeners.get(i).blackboardUpdated(bbSample);
		}
	}
}
