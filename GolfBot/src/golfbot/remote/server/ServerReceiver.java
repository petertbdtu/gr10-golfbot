package golfbot.remote.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import golfbot.sharedObjects.GyroSample;
import golfbot.sharedObjects.IRSample;
import golfbot.sharedObjects.DrivingMotorSample;
import golfbot.sharedObjects.SonicSample;
import golfbot.sharedObjects.TouchSample;

public class ServerReceiver extends Thread{
	
	private ArrayList<ConnectionWrapper> cwConnections;
	private ArrayList<ConnectionWrapper> cwToBeClosed;
	private Blackboard bb;

	public ServerReceiver() {
		super();
		cwConnections = new ArrayList<ConnectionWrapper>();
		cwToBeClosed = new ArrayList<ConnectionWrapper>();
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
			Iterator<ConnectionWrapper> cwIterator = cwConnections.iterator();
			while(cwIterator.hasNext()) {
				ConnectionWrapper cw = cwIterator.next();
				
				Object obj = null;
				try { obj = cw.ois.readObject(); } 
				catch (ClassNotFoundException | IOException e) { 
					cwToBeClosed.add(cw);
					continue;
				}
				if(obj != null)
					receiveLogic(obj);
			}
			
			if( !cwToBeClosed.isEmpty() ) {
				cwIterator = cwToBeClosed.iterator();
				while(cwIterator.hasNext()) { 
					closeConnection(cwIterator.next());
					cwIterator.remove();
				}
			}
		}
	}
	
	private void receiveLogic(Object obj) {
		if (obj instanceof GyroSample) {
			System.out.println((GyroSample)obj);
		} 
		else if (obj instanceof IRSample) {
			System.out.println((IRSample)obj);
		}
		else if (obj instanceof DrivingMotorSample) {
			System.out.println((DrivingMotorSample)obj);
		}
		else if (obj instanceof SonicSample) {
			System.out.println((SonicSample)obj);
		}
		else if (obj instanceof TouchSample) {
			System.out.println((TouchSample)obj);
		}		
	}
	
	public void closeConnections() {
		Iterator<ConnectionWrapper> cwIterator = cwConnections.iterator();
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
