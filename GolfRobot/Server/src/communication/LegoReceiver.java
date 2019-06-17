package communication;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class LegoReceiver extends Thread {
	private ServerSocket serverSocket;
	private Socket nSocket;
	private Socket bSocket;
	
	private InputStream nStream;
	private InputStream bStream;
	
	private volatile boolean closeConnection = false;

	private Boolean isMoving;
	private Boolean isCollecting;
	
	public LegoReceiver() {
		isMoving = false;
		isCollecting = false;
	}
	
	public boolean connect(int port) {
		try {
			serverSocket = new ServerSocket(port);
			nSocket = serverSocket.accept();
			nStream = nSocket.getInputStream();
			bSocket = serverSocket.accept();
			bStream = bSocket.getInputStream();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void run() {
		while(socketsWorking() && !closeConnection) {
			try { readNavigator(); }
			catch (IOException e) { closeConnection = true; }
			
			try { readBallCollector(); }
			catch (IOException e) { closeConnection = true; }
		}
	}
	
	private boolean socketsWorking() {
		boolean working = true;
		if(working)
			working = (nSocket != null && nStream != null && !nSocket.isClosed() && nSocket.isConnected());
		if(working)
			working = (bSocket != null && bStream != null && !bSocket.isClosed() && bSocket.isConnected());
		return working;
	}
	
	private void closeConnections() {
		try { nStream.close(); } 
		catch (Exception e) { }
		
		try { bStream.close(); } 
		catch (Exception e) { }
		
		try { nSocket.close(); } 
		catch (Exception e) { }
		
		try { bSocket.close(); } 
		catch (Exception e) { }
		
		try { serverSocket.close(); } 
		catch (Exception e) { }
	}
	
	private void readNavigator() throws IOException {
		int rec = nStream.read();
		if (rec != -1) {
			isMoving = rec == 1;
		}
	}
	
	private void readBallCollector() throws IOException {
		int rec = bStream.read();
		if (rec != -1) {
			isCollecting = rec == 1;
		}
	}

	public synchronized boolean getIsMoving() {
		return isMoving;
	}

	public synchronized boolean getIsCollecting() {
		return isCollecting;
	}

	public void stopReceiver() {
		closeConnection = true;
		closeConnections();
	}
}
