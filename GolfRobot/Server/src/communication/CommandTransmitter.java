package communication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandTransmitter {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private OutputStream out;	
	private final byte[] cmdBytes = new byte[] {0b00000001, 0b00000010, 0b00000011, 0b00000100, 0b00000101};
	
	public boolean connect(int port) {
		try {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			out = socket.getOutputStream();
			return true;
		} catch (IOException e) { 
			e.printStackTrace(); 
			return false;
		}
	}
	
	public void closeConnections() {
		try { out.close(); }
		catch (IOException e) { e.printStackTrace(); }
		
		try { socket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { serverSocket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotTravel(double distance) {
		//Build Packet
		short cmdDist = (short) distance;
		byte[] cmd = new byte[3];
		cmd[0] = cmdBytes[0];
		cmd[1] = (byte)(cmdDist & 0xff);
		cmd[2] = (byte)((cmdDist >> 8) & 0xff);
		
		//Send Packet
		try { out.write(cmd); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotTurn(double angle) {
		//Build Packet
		short cmdAng = (short) angle;
		byte[] cmd = new byte[3];
		cmd[0] = cmdBytes[1];
		cmd[1] = (byte)(cmdAng & 0xff);
		cmd[2] = (byte)((cmdAng >> 8) & 0xff);
		
		//Send Packet
		try { out.write(cmd); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotStop() {
		//Build Packet
		byte[] cmd = new byte[3];
		cmd[0] = cmdBytes[2];
		cmd[1] = (byte)(0 & 0xff);
		cmd[2] = (byte)((0 >> 8) & 0xff);
		
		//Send Packet
		try { out.write(cmd); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotCollectBall() {
		//Build Packet
		byte[] cmd = new byte[3];
		cmd[0] = cmdBytes[3];
		cmd[1] = (byte)(0 & 0xff);
		cmd[2] = (byte)((0 >> 8) & 0xff);
		
		//Send Packet
		try { out.write(cmd); } 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void robotDeliverBalls() {
		//Build Packet
		byte[] cmd = new byte[4];
		cmd[0] = cmdBytes[0];
		cmd[1] = (byte)(0 & 0xff);
		cmd[2] = (byte)((0 >> 8) & 0xff);
		
		//Send Packet
		try { out.write(cmd); } 
		catch (IOException e) { e.printStackTrace(); }
	}

	public void stopTransmitter() {
		// TODO Auto-generated method stub
		
	}
	
}
