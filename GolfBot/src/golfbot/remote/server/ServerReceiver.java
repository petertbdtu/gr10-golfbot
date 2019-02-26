package golfbot.remote.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import golfbot.samples.DrivingMotorSample;
import golfbot.samples.GyroSample;
import golfbot.samples.IRSample;
import golfbot.samples.SonicSample;
import golfbot.samples.TouchSample;

public class ServerReceiver extends Thread {
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
	
	private ArrayList<ConnectionWrapper> cwConnections;
	private ArrayList<ConnectionWrapper> cwToBeClosed;
	private BlackboardSample bbSample;
	private boolean blackboardUpdate;
	private Blackboard bb;

	public ServerReceiver() {
		super();
		blackboardUpdate = false;
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
				if(obj != null) {
					blackboardUpdate = true;
					receiveLogic(obj);
				}
			}
			
			if(blackboardUpdate) {
				bb.setBlackboardSample(bbSample);
				blackboardUpdate = false;
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
			GyroSample gs = (GyroSample)obj;
			bbSample.sGyroAngle = gs.angle;
			bbSample.sGyroRate = gs.rate;
		} 
		else if (obj instanceof IRSample) {
			IRSample irs = (IRSample)obj;
			bbSample.sIRDistance = irs.distance;
		}
		else if (obj instanceof DrivingMotorSample) {
			DrivingMotorSample dms = (DrivingMotorSample)obj;
			bbSample.mLeftDrivingTacho = dms.leftTachoCount;
			bbSample.mRightDrivingTacho = dms.rightTachoCount;
		}
		else if (obj instanceof SonicSample) {
			SonicSample ss = (SonicSample)obj;
			bbSample.sSonicDistance = ss.distance;
		}
		else if (obj instanceof TouchSample) {
			TouchSample ts = (TouchSample)obj;
			bbSample.sTouchPressed = ts.pressed;
		}		
	}
	
	public void closeConnections() {
		Iterator<ConnectionWrapper> cwIterator = cwConnections.iterator();
		while(cwIterator.hasNext()) {
			closeConnection(cwIterator.next());
			cwIterator.remove();
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
}
