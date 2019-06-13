package communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import mapping.LidarScan;

public class LidarReceiver extends Thread {

	private DatagramSocket socket;
	private boolean debug;
	private double lastAngle = 0;
	private boolean switcher = false;
	private boolean newData = false;
	private LidarScan scan1 = new LidarScan();
	private LidarScan scan2 = new LidarScan();
	private LidarScan tempScan = new LidarScan();
	
	public LidarReceiver() {
		this.debug = false;
	}
	
	public LidarReceiver(boolean debug) {
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
			byte[] buffer = new byte[1000];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try { socket.receive(packet); } 
			catch (IOException e) { break; }
			byte[] data = packet.getData();
			//System.out.println(data.length);
			decryptPacket(data);
		}
	}
	
	// Returns the newest complete scan
	public LidarScan getScan() {
		if(newData) {
			switcher = !switcher;
			newData = false;
		}
		if (switcher) {	return scan1; }
		else { return scan2; }
	}
	
	public void decryptPacket(byte[] buffer) {
		// Check for Zero Packet - Skip if true
		int ct = (buffer[0] & 0xFF);
		if(ct == 1) {
			if(debug) { System.out.println("Skipping packet... (ZeroPacket)"); }
			return;
		}
		
		// Data offset
		int sampleStartIndex = 8;
		
		// Amount of samples
		int samples = (buffer[1] & 0xFF);
		if(debug) { System.out.println("Samples in packet: " + samples); }
		
		// Calculate start/end values
		double start_distance = binaryToDistance(buffer[sampleStartIndex], buffer[sampleStartIndex+1]);
		double start_angle = binaryToAngle(buffer[2], buffer[3], start_distance);
		double end_distance = binaryToDistance(buffer[sampleStartIndex + (samples-1)*2], buffer[sampleStartIndex + (samples-1)*2+1]);
		double end_angle = binaryToAngle(buffer[4], buffer[5], end_distance);
		if(debug) { System.out.println("Angle interval: [" + start_angle + ":" + end_angle + "]"); }
		if(debug) { System.out.println("Distance interval: [" + start_distance + ":" + end_distance + "]"); }
		
		// Angle difference between start and end
		double diff_angle = getAngleDifference(start_angle, end_angle);
		if(debug) { System.out.println("Angle difference: " + diff_angle); }
		
		// Calculate distance and angle for each sample
		for (int i = 0 ; i < samples ; i++) {
			int index = i*2+sampleStartIndex;
			
			// Calculate distance
			double distance = binaryToDistance(buffer[index],buffer[index+1]);
			if(debug) { System.out.println("Calculated distance: " + distance); }

			
			// Skip if zero-distance
			if(distance >= 0 && distance < 0.01) {
				if(debug) { System.out.println("Skipping sample... (ZeroSample)"); }
				continue;
			}
			
			// Calculate angle
			double calculated_angle = diff_angle / samples * i + start_angle;
			double distCorrection = distanceCorrection(distance);
			double angle;
			if(calculated_angle + distCorrection < 0) {
				angle = 360 + distCorrection;
			} else {
				angle = calculated_angle + distCorrection;
			}
			angle = angle % 360;
			
			if(debug) { System.out.println("Calculated angle: " + angle); }

			if( 	!(angle > 115.0 && angle < 160.0) 	// Venstre Forhjul
				&& 	!(angle > 200.0 && angle < 245.0) 	// Højre Forhjul (måske)
				&& 	!(angle > 325.0 || angle < 35.0) 	// Baghjul
				) {

				// Collects 0-360 degree scan's
				if(lastAngle > angle + 10) {
					//System.out.println("Scan finished... Found " + tempScan.scanSize() + " samples...");
					if(debug) { System.out.println("Scan finished... Found " + tempScan.scanSize() + " samples..."); }
					if(switcher) {
						scan2 = new LidarScan(tempScan);
						if(!newData) { newData = true; }
					} else {
						scan1 = new LidarScan(tempScan);
						if(!newData) { newData = true; }
					}
					tempScan.clear();
				}
				lastAngle = angle;
				tempScan.addSample(angle, distance);
			}
		}
	}
	
	// Transforms distance bytes to short
	private double binaryToDistance(byte b2, byte b1) {
		return (((int)b1 & 0xFF) << 8 | (b2 & 0xFF)) / 4.0;
	}
	
	// Calculates angular correction given a distance
	private double distanceCorrection(double distance) {
		double correction = 0;
		if (distance != 0.0)
			correction = Math.toDegrees(Math.atan(21.8 * ((155.3-distance)/(155.3*distance))));
		if (correction < 0)
			return 0;
		return correction;
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
