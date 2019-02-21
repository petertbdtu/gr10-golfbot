package golfbot.remote.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class RemoteMain {

	public static void main(String[] args) {
		int port = 3000;
		ServerTransmitter st = new ServerTransmitter(port++);
		if(st.connect()) {
			ServerReceiver sr = startReceiver(port);
			Scanner sc = new Scanner(System.in);
			String input = null;
			while(true) {
				input = sc.nextLine();
				
				if(input.toLowerCase().equals("exit")) {
					break;
				}
				
				if(input != null) {
					st.sendObject(input);
				}
			}
			sc.close();
			sr.closeConnections();
		}
		st.closeConnection();
	}
	
	public static ServerReceiver startReceiver(int port) {
		ServerReceiver sr = new ServerReceiver();
		ServerSocket serverSocket = null;
		Socket socket = null;
		
		for(int i = 0 ; i < 3 ; i++) {
			try {
				serverSocket = new ServerSocket(port++);
				socket = serverSocket.accept();
				sr.addSocket(serverSocket, socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sr;
	}

}
