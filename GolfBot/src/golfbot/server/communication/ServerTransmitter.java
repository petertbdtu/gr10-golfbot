package golfbot.server.communication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ServerTransmitter extends Thread {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private ObjectOutputStream oos;
	private int port;
	private LinkedList<Object> transferBuffer;
	
	public ServerTransmitter(int port) {
		super();
		this.port = port;
		this.transferBuffer = new LinkedList<Object>();
	}
	
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
	
	public void closeConnection() {
		try { oos.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { socket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { serverSocket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void sendObject(Object obj) {
		transferBuffer.add(obj);
	}
	
	@Override
	public void run() {
		while(!socket.isClosed() && socket.isConnected()) {
			Object obj = transferBuffer.poll();
			if(obj != null) {
				try {
					oos.writeObject(obj);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}
		closeConnection();
	}
	
}
