package Navigation;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import lejos.utility.Delay;

public abstract class KnowledgeSource<E> extends Thread {
	
	private Socket socket;
	private ObjectOutputStream oos;
	
	public boolean connect(String ip, int port) {
		try {
			socket = new Socket(ip,port);
			oos = new ObjectOutputStream(socket.getOutputStream());
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
			E knowledge = getKnowledge();
			if(knowledge != null) {
				try {
					oos.writeObject(knowledge);
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
	
	public void closeConnection() {
		try { oos.close(); } 
		catch (IOException e) {}
		
		try { socket.close(); } 
		catch (IOException e) {}
	}
	
}
