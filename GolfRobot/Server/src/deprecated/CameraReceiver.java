package deprecated;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;



public class CameraReceiver extends Thread {

	private ServerSocket serverSocket;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private volatile byte[] img = new byte[1];
	private volatile boolean keepAlive = true;
	
	public boolean connect(int port) {
		try { 
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (Exception e) { 
			return false; 
		}
		return true;
	}
	
	@Override
	public void run() {
		while(keepAlive) {
				try {
				byte[] size_buff = new byte[4];
	            is.read(size_buff);
	            int size = ByteBuffer.wrap(size_buff).asIntBuffer().get();
	            os.write(size_buff);
	            
	            byte[] msg_buff = new byte[1024];
	            byte[] img_buff = new byte[size];
	            int img_offset = 0;
	            while(true) {
	                int bytes_read = is.read(msg_buff, 0, msg_buff.length);
	                if(bytes_read == -1) { break; }
	
	                // Copy bytes into img_buff
	                System.arraycopy(msg_buff, 0, img_buff, img_offset, bytes_read);
	                img_offset += bytes_read;	
	                if(img_offset >= size) { break; }
	            }
	            
	            img = img_buff.clone();
	            
	            byte[] OK = new byte[] {0x4F, 0x4B};
				os.write(OK);
			} catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	private int getBytesAsInt(byte[] bytes, int start) {
		int tmp = ((bytes[start] & 0xff) << 24) | ((bytes[start+1] & 0xff) << 16) | ((bytes[start+2] & 0xff) << 8) | (bytes[start+3] & 0xff);
		return tmp;
	}
	
//	private void decryptPacket(int bytesToRead, int packetSize) {
//		byte[] imgBuffer = new byte[bytesToRead];
//		int count = 0;
//		
//		for(int i = 0; i < bytesToRead; i+=packetSize, count++) {
//			byte[] tmpBuf = new byte[packetSize];
//			
//			DatagramPacket packet = new DatagramPacket(tmpBuf, packetSize);
//			try { socket.receive(packet); } 
//			catch (IOException e) {}
//			
//			for(int j = count*packetSize; j < Math.min((count+1)*packetSize,bytesToRead); j++) {
//				imgBuffer[j] = packet.getData()[j%packetSize];
//			}			
//		}
//		
//		img = imgBuffer.clone();
//	}
	
	public byte[] getImage() {
		return img;
	}

	public void stopReceiver() {
		keepAlive = false;
	}
}
