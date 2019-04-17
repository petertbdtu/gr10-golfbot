package golfbot.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import golfbot.server.communication.LegoReceiver;
import golfbot.server.communication.ServerTransmitter;

public class RemoteMain {

	public static void main(String[] args) {
		int port = 3000;
		ServerTransmitter st = new ServerTransmitter(port++);
		
		LegoReceiver sr = null;
		Scanner sc = null;
		
		if(st.connect()) {
			st.start();
			sr = startReceiver(port);
			sc = new Scanner(System.in);
			
			while(true) {
				String input = sc.nextLine();
				
				if(input.toLowerCase().equals("exit")) {
					break;
				}
				
				if(input != null) {
					st.sendObject(input);
				}
			}
			
			sc.close();
			sr.closeConnections();
			st.closeConnection();
		}
	}
	
	public static LegoReceiver startReceiver(int port) {
		LegoReceiver sr = new LegoReceiver();
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
		sr.start();
		return sr;
	}

}
