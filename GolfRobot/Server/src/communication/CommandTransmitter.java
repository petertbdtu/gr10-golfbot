package communication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import blackboard.BlackboardListener;
import blackboard.BlackboardSample;

public class CommandTransmitter implements BlackboardListener {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private ObjectOutputStream oos;
	private BlackboardSample bbSample = null;
	
	private double angularCorrection = 0.0;
	private double distanceCorrection = 0.0;
	
	public boolean connect(int port) {
		try {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			oos = new ObjectOutputStream(socket.getOutputStream());
			return true;
		} catch (IOException e) { 
			e.printStackTrace(); 
			return false;
		}
	}
	
	public void closeConnections() {
		try { oos.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { socket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { serverSocket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotTravel(double angle, double distance) {
		if (angle != 0) {
			angle += angle < 0 ? -angularCorrection : angularCorrection;
		}
		
		if (distance != 0) {
			distance += distanceCorrection;
		}
		try { oos.writeObject("M " + -angle + ":" + -distance); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotStop() {
		try { oos.writeObject("S"); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotCollectBall() {
		try { oos.writeObject("B"); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotSlowDown() {
		try { oos.writeObject("J"); }
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public BlackboardSample getSample() {
		return bbSample;
	}

	@Override
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = bbSample;
	}
	
}
