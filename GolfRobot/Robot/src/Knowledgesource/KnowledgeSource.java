package Knowledgesource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import lejos.utility.Delay;

public abstract class KnowledgeSource<E> extends Thread {
	
	private Socket socket;
	private OutputStream os;
	
	public boolean connect(String ip, int port) {
		try {
			socket = new Socket(ip,port);
			os = socket.getOutputStream();
			this.start();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void run() {
		while(!socket.isClosed() && socket.isConnected()) {
			byte[] data = getKnowledgeAsBytes();
			if(data.length > 0) {
				try {
					os.write(data);
					Delay.msDelay(50);
				} catch (IOException e) {
					closeConnection();
					break;
				}
			}
			Delay.msDelay(50);
		}
	}

	protected abstract E getKnowledge();
	protected abstract byte[] getKnowledgeAsBytes();
	
	public void closeConnection() {
		try { os.close(); } 
		catch (IOException e) {}
		
		try { socket.close(); } 
		catch (IOException e) {}
	}
	
}
