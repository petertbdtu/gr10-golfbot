package golfbot.robot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import golfbot.robot.knowledgesources.KSBallManagement;
import golfbot.robot.knowledgesources.KSNavigation;

public class CommandReceiver {
	
	private String[] move = new String[2];
	private final String IP = "192.168.0.101";
	private int port = 3000;
	private Socket socket = null;
	private ObjectInputStream ois = null;
	
	KSNavigation navigation = RobotSingle.navigation;
	KSBallManagement manager = RobotSingle.manager;
	
	public CommandReceiver(KSNavigation navigation, KSBallManagement manager) {
		this.navigation = navigation;
		this.manager = manager;
		initReceiver();
	}
	
	public void initReceiver() {
		try {
			socket = new Socket(IP,port++);
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Main Receiver : Couldn't connect");
		}
	}
	
	public void runCommand() {
		while(!socket.isClosed() && socket.isConnected()) {
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
					navigation.travelTo(Double.parseDouble(move[0]), Double.parseDouble(move[1]));
					break;
				//For stopping robot send a msg (String) "S"	
				case 'S':
					navigation.stopMoving();
					break;
				//So far for collection ball send msg (String) "B"
				case 'B':
					//collect ball logic here
					
					//Test
					System.out.println("Collecting ball ....");
					break;
			}
		}
	}
		
	
}
