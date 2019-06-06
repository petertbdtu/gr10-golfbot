package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import objects.Pose;

public class LegoReceiver extends Thread {
	private ServerSocket lServerSocket;
	private ServerSocket nServerSocket;
	private ServerSocket bServerSocket;
	
	private Socket lSocket;
	private Socket nSocket;
	private Socket bSocket;
	
	private ObjectInputStream lStream;
	private ObjectInputStream nStream;
	private ObjectInputStream bStream;
	
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
			nStream = new ObjectInputStream(nSocket.getInputStream());
			lServerSocket = new ServerSocket(lPort);
			lSocket = lServerSocket.accept();
			lStream = new ObjectInputStream(lSocket.getInputStream());
			bServerSocket = new ServerSocket(bPort);
			bSocket = bServerSocket.accept();
			bStream = new ObjectInputStream(bSocket.getInputStream());
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
		Object obj = null;
		try { obj = lStream.readObject(); } 
		catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
		if(obj != null) {
			if(poseSwitcher) { 
				Pose temp = (Pose) obj; 
				pose2 = new Pose(temp.point.x,temp.point.y,temp.heading); 
				poseNewData = true;
			} else { 
				Pose temp = (Pose) obj; 
				pose1 = new Pose(temp.point.x,temp.point.y,temp.heading); 
				poseNewData = true;
			}
		}
	}
	
	private void readNavigator() {
		Object obj = null;
		try { obj = nStream.readObject(); } 
		catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
		if(obj != null) {
			System.out.println((Boolean)obj);
			if(movingSwitcher) { 
				isMoving2 = (Boolean) obj; 
				movingNewData = true;
			} else { 
				isMoving1 = (Boolean) obj; 
				movingNewData = true;
			}
		}
	}
	
	private void readBallCollector() {
		Object obj = null;
		try { obj = bStream.readObject(); } 
		catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
		if(obj != null) {
			if(collectSwitcher) { 
				isCollecting2 = (Boolean) obj; 
				collectNewData = true;
			} else { 
				isCollecting1 = (Boolean) obj; 
				collectNewData = true;
			}
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
