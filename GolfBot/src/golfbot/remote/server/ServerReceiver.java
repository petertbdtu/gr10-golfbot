package golfbot.remote.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerReceiver extends Thread{
	
	private ArrayList<ConnectionWrapper> lConnections;
	private Blackboard bb;

	public ServerReceiver() {
		super();
		lConnections = new ArrayList<ConnectionWrapper>();
	}
	
	public void addSocket(ServerSocket serverSocket, Socket socket) {
		try {
			ConnectionWrapper cw = new ConnectionWrapper(serverSocket, socket);
			lConnections.add(cw);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	@Override
	public void run() {
		int count = 0;
		Blackboard bb = Blackboard.getInstance();
		while(lConnections.size() > 0) {
			if(count == lConnections.size()-1) { count = 0; }
			ConnectionWrapper cw = lConnections.get(count);
			Object obj = null;
			try { obj = cw.ois.readObject(); } 
			catch (ClassNotFoundException | IOException e) { 
				closeConnection(cw);
				lConnections.remove(count);
				continue;
			}
			if(obj != null)
				receiveLogic(obj);
		}
	}
	
	private void receiveLogic(Object obj) {
		if (obj instanceof Blackboard) {
			
		} 
		else if (obj instanceof String) {
			
		}
	}
	
	public void closeConnections() {
		for(ConnectionWrapper cw : lConnections) {
			closeConnection(cw);
		}
	}
	
	private void closeConnection(ConnectionWrapper cw) {
		try { cw.ois.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { cw.socket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { cw.serverSocket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	private class ConnectionWrapper {
		ServerSocket serverSocket;
		Socket socket;
		ObjectInputStream ois;
		
		public ConnectionWrapper(ServerSocket serverSocket, Socket socket) throws IOException {
			this.serverSocket = serverSocket;
			this.socket = socket;
			this.ois = new ObjectInputStream(socket.getInputStream());
		}
	}
}
