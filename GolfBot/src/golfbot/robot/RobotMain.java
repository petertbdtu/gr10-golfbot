package golfbot.robot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class RobotMain {

	public static void main(String[] args) {
		String ip = "192.168.0.101";
		int port = 3000;
		Socket socket = null;
		ObjectInputStream ois = null;
		
		try {
			socket = new Socket(ip,port++);
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("couldn't create main connection");
			e.printStackTrace();
		}
		
		KSTest kst1 = new KSTest("hi");
		if(!kst1.connect("localhost", 3001)) {
			System.out.println("couldn't connect kst1");
		}
		
		KSTest kst2 = new KSTest("hiii");
		if(!kst2.connect("localhost", 3002)) {
			System.out.println("couldn't connect kst2");
		}
		
		KSTest kst3 = new KSTest("hiiiiii");
		if(!kst3.connect("localhost", 3003)) {
			System.out.println("couldn't connect kst3");
		}
		
		System.out.println("Listening.");
		System.out.println("Listening..");
		System.out.println("Listening...");
		while(!socket.isClosed() && socket.isConnected()) {
			String msg = null;
			try {
				msg = (String) ois.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				break;
			}
			
			if(msg != null) {
				System.out.println("Recieved: " + msg);
			}
			
		}
		
		try { ois.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { socket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
	}
}
