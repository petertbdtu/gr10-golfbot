package communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import blackboard.BlackboardListener;
import blackboard.BlackboardSample;

public class CommandTransmitter implements BlackboardListener {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private DataOutputStream dout;
	private ObjectOutputStream oos;
	private BlackboardSample bbSample = null;
	
	private final byte[] cmdBytes = new byte[] {0b00000001, 0b00000010, 0b00000011, 0b00000100, 0b00000101};
	
	private double angularCorrection = 0.0;
	private double distanceCorrection = 0.0;
	
	public boolean connect(int port) {
		try {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			dout = new DataOutputStream(socket.getOutputStream());
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
		
		try { dout.close(); }
		catch (IOException e) { e.printStackTrace(); }
		
		try { socket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { serverSocket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotTravel(double distance) {
		if (distance != 0) {
			distance += distanceCorrection;
		}
		short cmdDist = (short) distance;
		
		int cmd = (cmdBytes[0] << 16) + cmdDist;
		
		try { dout.write(cmd); } catch (IOException e) { e.printStackTrace(); }
		
		//try { oos.writeObject("M " + angle + ":" + distance); } 
		//catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotTurn(double angle) {
		if (angle != 0) {
			angle += angle < 0 ? -angularCorrection : angularCorrection;
		}
		
		short cmdAng = (short) angle;
		
		int cmd = (cmdBytes[1] << 16) + cmdAng;
		
		try { dout.write(cmd); } catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotStop() {
		
		//int cmdDummy = 0;
		
		int cmd = (cmdBytes[2]);
		
		try { dout.write(cmd); } catch (IOException e) { e.printStackTrace(); }
		
		//try { oos.writeObject("S"); } 
		//catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotCollectBall() {

		int cmdDummy = 0;
		
		int cmd = (cmdBytes[3]);
		
		try { dout.write(cmd); } catch (IOException e) { e.printStackTrace(); }
		
		//try { oos.writeObject("B"); } 
		//catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotSlowDown() {

		int cmdDummy = 0;
		
		int cmd = (cmdBytes[4]);
		
		try { dout.write(cmd); } catch (IOException e) { e.printStackTrace(); }
		
		//try { oos.writeObject("J"); }
		//catch (IOException e) { e.printStackTrace(); }
	}
	
	public BlackboardSample getSample() {
		return bbSample;
	}

	@Override
	public void blackboardUpdated(BlackboardSample bbSample) {
		this.bbSample = bbSample;
	}
	
}
