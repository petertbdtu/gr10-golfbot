package communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class CameraReceiver extends Thread {

	private DatagramSocket socket;
	private boolean debug;
	private double lastAngle = 0;
	private boolean switcher = false;
	private boolean newData = false;
	private LidarScan scan1 = new LidarScan();
	private LidarScan scan2 = new LidarScan();
	private LidarScan tempScan = new LidarScan();
	
	public CameraReceiver() {
		this.debug = false;
	}
	
	public CameraReceiver(boolean debug) {
		this.debug = debug;
	}
	
	public boolean bindSocket(int port) {
		try { socket = new DatagramSocket(port); } 
		catch (SocketException e) { return false; }
		return true;
	}
	
	@Override
	public void run() {
		while(socket != null && socket.isBound()) {
			byte[] buffer = new byte[100];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try { socket.receive(packet); } 
			catch (IOException e) { break; }
			//decryptPacket(packet.getData());
		}
	}
	
	public static void main(String[] args) {
		System.out.println("test");
	}
}
