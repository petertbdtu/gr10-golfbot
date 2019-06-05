package Navigation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class CommandReceiver extends Thread {
	
	private Socket socket = null;
	private ObjectInputStream ois = null;
	
	KSNavigation navigation;
	KSBallManagement manager;
	
	public CommandReceiver(KSNavigation navigation, KSBallManagement manager) {
		this.navigation = navigation;
		this.manager = manager;
	}
	
	public boolean connect(String ip, int port) {
		try {
			socket = new Socket(ip,port++);
			ois = new ObjectInputStream(socket.getInputStream());
			return true;
		} catch (IOException e) {
			System.out.println("Main Receiver : Couldn't connect");
			return false;
		}
	}
	
	@Override
	public void run() {
		String[] move = new String[2];
		while(socket != null && ois != null && !socket.isClosed() && socket.isConnected()) {
			String msg = null;
			try { msg = (String) ois.readObject(); } 
			catch (ClassNotFoundException | IOException e) { break;	}
			switch(msg.charAt(0)) {
				/*
				For moving the robot send a msg (String) "M angle:distance" where angle is the angle to turn in start, 
			    and distance is the distance to travel after the turn (turn/distance can be 0) they're both doubles
				*/
				case 'M':
					String newMsg = msg.substring(2, msg.length());
					move = newMsg.split(":");
					double angle = Double.parseDouble(move[0]);
					double distance = Double.parseDouble(move[1]);
					
					if(angle != 0) {
						navigation.turn(angle);
					} else if(distance != 0) {
						navigation.forward(distance);
					}
					break;
				//For stopping robot send a msg (String) "S"	
				case 'S':
					navigation.stopMoving();
					break;
				//So far for collection ball send msg (String) "B"
				case 'B':
					manager.pickup();
					System.out.println("Collecting ball ....");
					break;
			}
		}
	}	
}
