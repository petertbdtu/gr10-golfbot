package golfbot.server.communication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class CommandTransmitter {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private ObjectOutputStream oos;
	private final int port = 3000;
	
	public boolean connect() {
		boolean connected = false;
		try {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			oos = new ObjectOutputStream(socket.getOutputStream());
			connected = true;
		} catch (IOException e) { 
			e.printStackTrace(); 
		}
		return connected;
	}
	
	public void closeConnections() {
		try { oos.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { socket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { serverSocket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotTravel(double angle, double distance) {
		
		try {
			oos.writeObject("M " + angle + ":" + distance);
		} catch (IOException e) {
			e.printStackTrace();
			closeConnections();
		}
	}
	
	public void robotStop() {
		try {
			oos.writeObject("S");
		} catch (IOException e) {
			e.printStackTrace();
			closeConnections();
		}
	}
	
	public void robotCollectBall() {
		try {
			oos.writeObject("B");
		} catch (IOException e) {
			e.printStackTrace();
			closeConnections();
		}
	}
	
}
