package robot;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import Knowledgesource.KSBallManagement;
import Knowledgesource.KSNavigation;
import lejos.hardware.Sound;
import lejos.utility.Delay;

public class CommandReceiver extends Thread {
	
	private Socket socket = null;
	private InputStream dis = null;
	KSNavigation navigation;
	KSBallManagement manager;
	
	public CommandReceiver(KSNavigation navigation, KSBallManagement manager) {
		this.navigation = navigation;
		this.manager = manager;
	}
	
	public boolean connect(String ip, int port) {
		try {
			socket = new Socket(ip,port++);
			dis = socket.getInputStream();
			return true;
		} catch (IOException e) {
			System.out.println("Main Receiver : Couldn't connect");
			return false;
		}
	}
	
	@Override
	public void run() {
		while(socket != null && dis != null && !socket.isClosed() && socket.isConnected()) {
			int cmd;
			byte[] cmdBuff = new byte[3];
			try { cmd = dis.read(cmdBuff);} catch (IOException e) {e.printStackTrace(); break;}
			switch(cmdBuff[0]) {
				case 1:
					short readDist = 0;
							
					readDist = (short) (((cmdBuff[2] & 0xff) << 8) + (cmdBuff[1] & 0xff));
					if(readDist != 0) {navigation.forward(readDist);}
					
					break;
				case 2:
					short readAng = 0;
					readAng = (short) (((cmdBuff[2] & 0xff) << 8) + (cmdBuff[1] & 0xff));
					if(readAng != 0) {navigation.turn(readAng);	} 
					break;
				case 3:
					navigation.stopMoving();
					break;
				case 4:
					manager.pickup();
					break;
				case 5:
					manager.deliverBalls();
					Delay.msDelay(3000);
					Sound.setVolume(100);
					Sound.playSample(new File("flot.wav"));
					break;
				case 0:
					//filler byte
					break;
			}
		}
	}
//	
//	@Override
//	public void run() {
//		String[] move = new String[2];
//		while(socket != null && ois != null && !socket.isClosed() && socket.isConnected()) {
//			String msg = null;
//			try { msg = (String) ois.readObject(); } 
//			catch (ClassNotFoundException | IOException e) { break;	}
//			switch(msg.charAt(0)) {
//				/*
//				For moving the robot send a msg (String) "M angle:distance" where angle is the angle to turn in start, 
//			    and distance is the distance to travel after the turn (turn/distance can be 0) they're both doubles
//				*/
//				case 'M':
//					String newMsg = msg.substring(2, msg.length());
//					move = newMsg.split(":");
//					double angle = Double.parseDouble(move[0]);
//					double distance = Double.parseDouble(move[1]);
//					
//					if(angle != 0) {
//						navigation.turn(angle);
//					} else if(distance != 0) {
//						navigation.forward(distance);
//					}
//					break;
//				//For stopping robot send a msg (String) "S"	
//				case 'S':
//					navigation.stopMoving();
//					break;
//				//So far for collection ball send msg (String) "B"
//				case 'B':
//					manager.pickup();
//					System.out.println("Collecting ball ....");
//					break;
//			}
//		}
//	}	
}
