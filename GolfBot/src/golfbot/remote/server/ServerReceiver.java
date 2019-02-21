package golfbot.remote.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerReceiver extends Thread{
	
	private ArrayList<ConnectionWrapper> cwConnections;
	private Iterator<ConnectionWrapper> cwIterator;
	private Blackboard bb;

	public ServerReceiver() {
		super();
		cwConnections = new ArrayList<ConnectionWrapper>();
	}
	
	public void addSocket(ServerSocket serverSocket, Socket socket) {
		try {
			ConnectionWrapper cw = new ConnectionWrapper(serverSocket, socket);
			cwConnections.add(cw);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	@Override
	public void run() {
		bb = Blackboard.getInstance();
		
		while(!cwConnections.isEmpty()) {
			cwIterator = cwConnections.iterator();
			while(cwIterator.hasNext()) {
				ConnectionWrapper cw = cwIterator.next();
				
				Object obj = null;
				try { obj = cw.ois.readObject(); } 
				catch (ClassNotFoundException | IOException e) { 
					cwIterator.remove();
					closeConnection(cw);
					continue;
				}
				if(obj != null)
					receiveLogic(obj);
			}
		}
	}
	
	private void receiveLogic(Object obj) {
		if (obj instanceof Blackboard) {
			bb.getClass();
		} 
		else if (obj instanceof String) {
			System.out.println((String)obj);
		}
	}
	
	public void closeConnections() {
		cwIterator = cwConnections.iterator();
		while(cwIterator.hasNext()) {
			ConnectionWrapper cw = cwIterator.next();
			cwIterator.remove();
			closeConnection(cw);
		}
	}
	
	private void closeConnection(ConnectionWrapper cw) {
		try { cw.ois.close(); } 
		catch (IOException e) {}
		
		try { cw.socket.close(); } 
		catch (IOException e) {}
		
		try { cw.serverSocket.close(); } 
		catch (IOException e) {}
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
