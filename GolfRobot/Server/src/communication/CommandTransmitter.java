package communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import blackboard.BlackboardListener;
import blackboard.BlackboardSample;

public class CommandTransmitter implements BlackboardListener {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private OutputStream dout;
	private ObjectOutputStream oos;
	private BlackboardSample bbSample = null;
	
	private final byte[] cmdBytes = new byte[] {0b00000001, 0b00000010, 0b00000011, 0b00000100, 0b00000101};
	
	private double angularCorrection = 0.0;
	private double distanceCorrection = 0.0;
	
	public boolean connect(int port) {
		try {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			dout = socket.getOutputStream();
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
		
		//int cmd = (cmdBytes[0] << 16) + cmdDist;
		byte[] cmd = new byte[3];
		cmd[0] = cmdBytes[0];
		cmd[1] = (byte)(cmdDist & 0xff);
		cmd[2] = (byte)((cmdDist >> 8) & 0xff);
		try { dout.write(cmd); } catch (IOException e) { e.printStackTrace(); }
		
		//try { oos.writeObject("M " + angle + ":" + distance); } 
		//catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotTurn(double angle) {
		if (angle != 0) {
			angle += angle < 0 ? -angularCorrection : angularCorrection;
		}
		
		short cmdAng = (short) angle;
		
		byte[] cmd = new byte[3];
		cmd[0] = cmdBytes[1];
		cmd[1] = (byte)(cmdAng & 0xff);
		cmd[2] = (byte)((cmdAng >> 8) & 0xff);
		
		try { dout.write(cmd); } catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotStop() {
		
		//int cmdDummy = 0;
		
		//int cmd = (cmdBytes[2]);
		
		byte[] cmd = new byte[3];
		cmd[0] = cmdBytes[2];
		cmd[1] = (byte)(0 & 0xff);
		cmd[2] = (byte)((0 >> 8) & 0xff);
		
		try { dout.write(cmd); } catch (IOException e) { e.printStackTrace(); }
		
		//try { oos.writeObject("S"); } 
		//catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotCollectBall() {

		int cmdDummy = 0;
		
		//int cmd = (cmdBytes[3]);
		byte[] cmd = new byte[3];
		cmd[0] = cmdBytes[3];
		cmd[1] = (byte)(0 & 0xff);
		cmd[2] = (byte)((0 >> 8) & 0xff);
		
		try { dout.write(cmd); } catch (IOException e) { e.printStackTrace(); }
		
		//try { oos.writeObject("B"); } 
		//catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotDeliverBalls() {

		int cmdDummy = 0;
		
		//int cmd = (cmdBytes[4]);
		
		byte[] cmd = new byte[4];
		cmd[0] = cmdBytes[0];
		cmd[1] = (byte)(0 & 0xff);
		cmd[2] = (byte)((0 >> 8) & 0xff);
		
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
