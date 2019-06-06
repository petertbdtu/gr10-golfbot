package communication;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import objects.Pose;

public class LegoReceiver extends Thread {
	private ServerSocket lServerSocket;
	private ServerSocket nServerSocket;
	private ServerSocket bServerSocket;
	
	private Socket lSocket;
	private Socket nSocket;
	private Socket bSocket;
	
	private InputStream lStream;
	private InputStream nStream;
	private InputStream bStream;
	
	private boolean poseSwitcher = false;
	private boolean movingSwitcher = false;
	private boolean collectSwitcher = false;

	private boolean poseNewData = false;
	private boolean movingNewData = false;
	private boolean collectNewData = false;

	private Pose pose1;
	private Pose pose2;
	private Boolean isMoving1;
	private Boolean isMoving2;
	private Boolean isCollecting1;
	private Boolean isCollecting2;
	
	public LegoReceiver() {
		pose1 = new Pose();
		isMoving1 = false;
		isCollecting1 = false;
		pose2 = new Pose();
		isMoving2 = false;
		isCollecting2 = false;
	}
	
	public boolean connect(int nPort, int lPort, int bPort) {
		try {
			nServerSocket = new ServerSocket(nPort);
			nSocket = nServerSocket.accept();
			nStream = nSocket.getInputStream();
			lServerSocket = new ServerSocket(lPort);
			lSocket = lServerSocket.accept();
			lStream = lSocket.getInputStream();
			bServerSocket = new ServerSocket(bPort);
			bSocket = bServerSocket.accept();
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
			readLocalization();
			readNavigator();
			readBallCollector();
		}
	}
	
	private boolean socketsWorking() {
		boolean working = true;
		if(working)
			working = (lSocket != null && lStream != null && !lSocket.isClosed() && lSocket.isConnected());
		if(working)
			working = (nSocket != null && nStream != null && !nSocket.isClosed() && nSocket.isConnected());
		if(working)
			working = (bSocket != null && bStream != null && !bSocket.isClosed() && bSocket.isConnected());
		return working;
	}
	
	private void readLocalization() {
		try {
			int rec = lStream.read();
			if (rec != -1) {
				
				//int x, y; float heading.
				ByteBuffer inbytes = ByteBuffer.allocate(12);
				lStream.read(inbytes.array());
				Pose pose = new Pose(inbytes.getInt(), inbytes.getInt(), inbytes.getFloat());
				
				if (poseSwitcher) {
					pose2 = pose;
				}
				else {
					pose1 = pose;
				}
				poseNewData = true;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
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
		
	public Pose getPose() {
		if(poseNewData) { 
			poseSwitcher = !poseSwitcher; 
			poseNewData = false;
		}
		if(poseSwitcher) { return pose1; }
		else { return pose2; }
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
