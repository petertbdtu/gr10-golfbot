package communication;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import mapping.LidarScan;

import mapping.LidarScan;

public class CameraReceiver extends Thread {

	private DatagramSocket socket;
	private boolean debug;
	private double lastAngle = 0;
	private boolean switcher = false;
	private boolean newData = false;
	//private BufferedImage img1 = new BufferedImage(0,0,0);
	//private BufferedImage img2 = new BufferedImage(0,0,0);
	//private BufferedImage tempImg = new BufferedImage(0,0,0);
	
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
			byte[] buffer = new byte[5];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try { socket.receive(packet); } 
			catch (IOException e) { break; }
		
			int packetSize = 1024;
			
			byte[] tis = new byte[5];
			
			tis = packet.getData();
			
			if((tis[0] & 0xFF) == 255) {
				int bytesToRead = getBytesAsInt(tis,1);
				System.out.println(bytesToRead);
				
				decryptPacket(bytesToRead, packetSize);
			}
			
		}
	}
	
	public static void main(String[] args) {
		System.out.println("test");
		CameraReceiver meme = new CameraReceiver();
		meme.bindSocket(6000);
		meme.run();
		
	}
	
	private int getBytesAsInt(byte[] bytes, int start) {
				
		int tmp = ((bytes[start] & 0xff) << 24) | ((bytes[start+1] & 0xff) << 16) | ((bytes[start+2] & 0xff) << 8) | (bytes[start+3] & 0xff);
		
		return tmp;
	}
	
	private int decryptPacket(int bytesToRead, int packetSize) {
		byte[] imgBuffer = new byte[bytesToRead];
		
		int count = 0, retval = 0;
		
		for(int i = 0; i < bytesToRead; i+=packetSize) {
			byte[] tmpBuf = new byte[packetSize];
			
			DatagramPacket packet = new DatagramPacket(tmpBuf, packetSize);
			try { socket.receive(packet); } 
			catch (IOException e) {
				retval = -1;
				e.printStackTrace();
			}
			
			for(int j = count*packetSize; j < Math.min((count+1)*packetSize,bytesToRead); j++) {
				imgBuffer[j] = packet.getData()[j%packetSize];
			}
			
			count++;
			
		}
		
		
		InputStream bis = new ByteArrayInputStream(imgBuffer);
		
	    try {
			BufferedImage bImage2 = ImageIO.read(bis); // read to image
		    ImageIO.write(bImage2, "jpg", new File("output.jpg") );
		    System.out.println("Wrote Image");
		} catch (Exception e) {
			retval = -1;
			e.printStackTrace();
		}
	    
	    return retval;
	}
}
