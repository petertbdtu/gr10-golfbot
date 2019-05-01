package golfbot.server.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class LidarReceiver extends Thread {

	private DatagramSocket socket;
	private double lastAngle = 0;
	private boolean switcher = false;
	private boolean newData = false;
	private HashMap<Double, Double> scan1 = new HashMap<Double, Double>();
	private HashMap<Double, Double> scan2 = new HashMap<Double, Double>();
	private HashMap<Double, Double> tempScan = new HashMap<Double, Double>();
	
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
			decryptPacket(packet.getData());
		}
	}
	
	// Returns the newest complete scan
	public HashMap<Double, Double> getScan() {
		if(newData) {
			switcher = !switcher;
			newData = false;
		}
		if (switcher) {	return scan1; }
		else { return scan2; }
	}
	
	public void decryptPacket(byte[] buffer) {
		StringBuilder sb = new StringBuilder(buffer.length * 2);
		for(byte b : buffer) {
			sb.append(String.format("%02x", b));
		}
		
		System.out.println(sb);
		
		// Check for Zero Packet - Skip if true
		int ct = (buffer[0] & 0xFF);
		if(ct == 1) {
			System.out.println("Skipping --> ZeroPacket");
			return;
		}
		
		// Data offset
		int sampleStartIndex = 8;
		
		// Amount of samples
		int samples = (buffer[1] & 0xFF);
		System.out.println("Samples: " + samples);
		
		// Calculate start values
		double start_distance = binaryToDistance(buffer[sampleStartIndex], buffer[sampleStartIndex+1]);
		double start_angle = binaryToAngle(buffer[2], buffer[3], start_distance);
		System.out.println("Start: [" + start_angle + ":" + start_distance + "] --> [" + String.format("%02x", buffer[2])+ String.format("%02x", buffer[3]) + "]");
		
		// Calculate end values
		double end_distance = binaryToDistance(buffer[sampleStartIndex + (samples-1)*2], buffer[sampleStartIndex + (samples-1)*2+1]);
		double end_angle = binaryToAngle(buffer[4], buffer[5], end_distance);
		System.out.println("End: [" + end_angle + ":" + end_distance + "] --> [" + String.format("%02x", buffer[4])+ String.format("%02x", buffer[5]) + "]");
		
		// Angle difference between start and end
		double diff_angle = getAngleDifference(start_angle, end_angle);
		System.out.println("Diff: [" + diff_angle + "]");
		
		// Calculate distance and angle for each sample
		for (int i = 0 ; i < samples ; i++) {
			int index = i*2+sampleStartIndex;
			
			// Calculate distance
			double distance = binaryToDistance(buffer[index],buffer[index+1]);
			System.out.println("Distance: [" + distance + "] --> " + String.format("%02x", buffer[index])+ String.format("%02x", buffer[index+1]));

			
			// Skip if zero-distance
			if(distance >= 0 && distance < 0.01) {
				System.out.println("Skipping --> ZeroSample --> " + String.format("%02x", buffer[index])+ String.format("%02x", buffer[index+1]));
				continue;
			}
			
			// Calculate angle
			double angle = (diff_angle / samples * i + start_angle + distanceCorrection(distance)) % 360;
			System.out.println("Angle: [" + angle + "]");
			// Collects 0-360 degree scan's
			if(lastAngle > angle) {
				System.out.println("SCAN ENDED \n");
				if(switcher) {
					scan2 = tempScan;
					newData = true;
				} else {
					scan1 = tempScan;
					newData = true;
				}
				tempScan.clear();
			}
			lastAngle = angle;
			tempScan.put(angle, distance);
		}
	}
	
	// Transforms distance bytes to short
	private double binaryToDistance(byte b2, byte b1) {
		return (((int)b1 & 0xFF) << 8 | (b2 & 0xFF)) / 4.0;
	}
	
	// Calculates angular correction given a distance
	private double distanceCorrection(double distance) {
		if (distance != 0.0)
			return Math.toDegrees(Math.atan(21.8 * ((155.3-distance)/(155.3*distance))));
		return 0;
	}
	
	// Transforms angle bytes to double (degrees)
	private double binaryToAngle(byte b2, byte b1, double distance) {
		int value = ((int)b1 & 0xFF) << 8 | (b2 & 0xFF);
		double dValue = value / 2.0 / 64.0;
		return (Math.abs(dValue + distanceCorrection(distance))) % 360;
	}
	
	//?
	private double getAngleDifference(double start_angle, double end_angle) {
		double diff_angle = Math.abs(end_angle - start_angle);
		if(diff_angle > 180)
			diff_angle = (end_angle + 360) - start_angle;
		return diff_angle;
	}
}
