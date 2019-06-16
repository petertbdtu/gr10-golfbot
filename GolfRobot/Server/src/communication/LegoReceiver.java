package communication;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import objects.Pose;

public class LegoReceiver extends Thread {
	private ServerSocket serverSocket;
	private Socket nSocket;
	private Socket bSocket;
	
	private InputStream nStream;
	private InputStream bStream;
	
	private volatile boolean movingSwitcher = false;
	private volatile boolean collectSwitcher = false;
	private boolean closeConnection = false;

	private Boolean isMoving1;
	private Boolean isMoving2;
	private Boolean isCollecting1;
	private Boolean isCollecting2;
	
	public LegoReceiver() {
		isMoving1 = false;
		isCollecting1 = false;
		isMoving2 = false;
		isCollecting2 = false;
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
			catch (IOException e) { }
			
			try { readBallCollector(); }
			catch (IOException e) { }
		}
		
		closeConnections();
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
		catch (IOException e) { }
		
		try { bStream.close(); } 
		catch (IOException e) { }
		
		try { nSocket.close(); } 
		catch (IOException e) { }
		
		try { bSocket.close(); } 
		catch (IOException e) { }
		
		try { serverSocket.close(); } 
		catch (IOException e) { }
	}
	
	private void readNavigator() throws IOException {
		int rec = nStream.read();
		if (rec != -1) {
			if (movingSwitcher)
				isMoving2 = rec == 1;
			else
				isMoving1 = rec == 1;
		}
	}
	
	private void readBallCollector() throws IOException {
		int rec = bStream.read();
		if (rec != -1) {
			if (collectSwitcher)
				isCollecting2 = rec == 1;
			else
				isCollecting1 = rec == 1;
		}
	}

	public Boolean getIsMoving() {
		movingSwitcher = !movingSwitcher; 
		if(movingSwitcher) { return isMoving1; }
		else { return isMoving2; }
	}

	public Boolean getIsCollecting() {
		collectSwitcher = !collectSwitcher;
		if(collectSwitcher) { return isCollecting1; }
		else { return isCollecting2; }
	}

	public void stopReceiver() {
		closeConnection = true;
	}
}
