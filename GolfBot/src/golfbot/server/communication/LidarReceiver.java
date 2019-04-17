package golfbot.server.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class LidarReceiver extends Thread {

	private DatagramSocket dSocket;
	private static final int PORT = 5000;
	private double lastAngle = 0;
	private boolean scanIsUpdated = false;
	private HashMap<Double, Double> scan = new HashMap<Double, Double>();
	private HashMap<Double, Double> newScan = new HashMap<Double, Double>();
	
	private boolean bindSocket() {
		try { dSocket = new DatagramSocket(PORT); } 
		catch (SocketException e) { return false; }
		return true;
	}

	@Override
	public void run() {
		boolean socketIsWorking = bindSocket();
		if(socketIsWorking)
			while(socketIsWorking) {
				byte[] buffer = new byte[100];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				try { dSocket.receive(packet); } 
				catch (IOException e) { socketIsWorking = false; }
				decryptPacket(packet.getData());
			}
		else
			System.out.println("LidarReceiver couldn't make socketbind");
	}
	
	private boolean decryptPacket(byte[] buffer) {
		// Check for Zero Packet - Skip if true
		int ct = buffer[0];
		if(ct == 1)
			return false;
		
		int sampleStartIndex = 8;
		int samples = buffer[1];
		double start_distance = binaryToDistance(buffer[sampleStartIndex], buffer[sampleStartIndex+1]);
		double start_angle = binaryToAngle(buffer[2], buffer[3], start_distance);
		double end_distance = binaryToDistance(buffer[sampleStartIndex + (samples-1)*2], buffer[sampleStartIndex + (samples-1)*2+1]);
		double end_angle = binaryToAngle(buffer[4], buffer[5], end_distance);
		double diff_angle = Math.abs(end_angle - start_angle);
		
		for (int i = 0 ; i < samples ; i++) {
			int index = i*2+sampleStartIndex;
			double distance = binaryToDistance(buffer[index],buffer[index+1]);
			if(distance >= 0 && distance < 0.01)
				continue;
			double angle = diff_angle / samples * i + start_angle + distanceCorrection(distance);
			System.out.println(String.format("[%5f:%d]",angle,(int)distance));			
			if(angle < 1.0 && angle > 359.0) {
				System.out.println("new scan available: ");
				scan = newScan;
				scanIsUpdated = true;
				newScan.clear();
			}
			lastAngle = angle;
			newScan.put(angle, distance);
		}
		return false;
	}
	
	private double binaryToDistance(byte b1, byte b2) {
		return ((((int)b1)&0xff) + (((int)b2)&0xff)) / 4.0;
	}
	
	private double distanceCorrection(double distance) {
		if (distance != 0)
			return Math.toDegrees(Math.atan(21.8 * ((155.3-distance)/(155.3*distance))));
		return 0;
	}
	
	private double binaryToAngle(byte b1, byte b2, double distance) {
		int value = ((((int)b1)&0xff) + (((int)b2)&0xff));
		return (value / 2.0 / 64.0) + distanceCorrection(distance);
	}
	
	public HashMap<Double,Double> getScan() {
		if(scanIsUpdated) {
			scanIsUpdated = false;
			return scan;
		}
		return null;
	}
	
}
