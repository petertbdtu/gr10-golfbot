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
	
	private boolean movingSwitcher = false;
	private boolean collectSwitcher = false;

	private boolean movingNewData = false;
	private boolean collectNewData = false;

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
		while(socketsWorking()) {
			readNavigator();
			readBallCollector();
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
	
	private void readNavigator() {
		try {
			int rec = nStream.read();
			if (rec != -1) {
				if (movingSwitcher) {
					isMoving2 = rec == 1;
				}
				else {
					isMoving1 = rec == 1;
				}
				movingNewData = true;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readBallCollector() {
		try {
			int rec = bStream.read();
			if (rec != -1) {
				if (collectSwitcher) {
					isCollecting2 = rec == 1;
				}
				else {
					isCollecting1 = rec == 1;
				}
				collectNewData = true;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Boolean getIsMoving() {
		if(movingNewData) { 
			movingSwitcher = !movingSwitcher; 
			movingNewData = false;
		}
		if(movingSwitcher) { return isMoving1; }
		else { return isMoving2; }
	}

	public Boolean getIsCollecting() {
		if(collectNewData) { 
			collectSwitcher = !collectSwitcher;
			collectNewData = false;
		}
		if(collectSwitcher) { return isCollecting1; }
		else { return isCollecting2; }
	}
}
