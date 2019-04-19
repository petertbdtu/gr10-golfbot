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
	private boolean switcher = false;
	private HashMap<Double, Double> scan1 = new HashMap<Double, Double>();
	private HashMap<Double, Double> scan2 = new HashMap<Double, Double>();
	private HashMap<Double, Double> tempScan = new HashMap<Double, Double>();
	
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
				catch (IOException e) { socketIsWorking = false;}
				decryptPacket(packet.getData());
			}
		else
			System.out.println("LidarReceiver couldn't make socketbind");
	}
	
	// Returns the newest complete scan
	public HashMap<Double, Double> getScan() {
		switcher = !switcher;
		if (switcher) {	return scan1; }
		else { return scan2; }
	}
	
	public void decryptPacket(byte[] buffer) {
		// Check for Zero Packet - Skip if true
		short ct = (short)(buffer[0] & 0xFF);
		if(ct == 1)
			return;
		
		// Data offset
		short sampleStartIndex = 8;
		
		// Amount of samples
		short samples = (short)(buffer[1] & 0xFF);
		
		// Calculate start values
		double start_distance = binaryToDistance(buffer[sampleStartIndex], buffer[sampleStartIndex+1]);
		double start_angle = binaryToAngle(buffer[2], buffer[3], start_distance);
		
		// Calculate end values
		double end_distance = binaryToDistance(buffer[sampleStartIndex + (samples-1)*2], buffer[sampleStartIndex + (samples-1)*2+1]);
		double end_angle = binaryToAngle(buffer[4], buffer[5], end_distance);
		
		// Angle difference between start and end
		double diff_angle = Math.abs(end_angle - start_angle);
		
		// Calculate distance and angle for each sample
		for (int i = 0 ; i < samples ; i++) {
			int index = i*2+sampleStartIndex;
			
			// Calculate distance
			double distance = binaryToDistance(buffer[index],buffer[index+1]);
			
			// Skip if zero-distance
			if(distance >= 0 && distance < 0.01)
				continue;
			
			// Calculate angle
			double angle = diff_angle / samples * i + start_angle + distanceCorrection(distance);
			
			// Collects 0-360 degree scan's
			if(angle < lastAngle) {
				System.out.println("new scan available: ");
				if(switcher) 
					scan2 = tempScan;
				else
					scan1 = tempScan;
				tempScan.clear();
			}
			lastAngle = angle;
			tempScan.put(angle, distance);
		}
	}
	
	// Transforms distance bytes to short
	private double binaryToDistance(byte b1, byte b2) {
		return ((short) (b1<<8 | b2 & 0xFF)) / 4.0;
	}
	
	// Calculates angular correction given a distance
	private double distanceCorrection(double distance) {
		if (distance != 0)
			return Math.toDegrees(Math.atan(21.8 * ((155.3-distance)/(155.3*distance))));
		return 0;
	}
	
	// Transforms angle bytes to double (degrees)
	private double binaryToAngle(byte b1, byte b2, double distance) {
		short value = ((short) (b1<<8 | b2 & 0xFF));
		return (value / 2.0 / 64.0) + distanceCorrection(distance);
	}
}
