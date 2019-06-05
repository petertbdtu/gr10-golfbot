package org.Server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import golfbot.samples.FrameSample;

public class TestKameraRemote {

	public static void main(String[] args) {
		int port = 3000;				
		ServerSocket serverSocket = null;
		Socket socket = null;
		ObjectInputStream ois = null;
		
		try {
			serverSocket = new ServerSocket(port++);
			socket = serverSocket.accept();
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream("FrameSampleFile.txt");
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        ObjectOutputStream objectOut = null;
		try {
			objectOut = new ObjectOutputStream(fileOut);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        try {
			while(!socket.isClosed() && socket.isConnected()) {			
				FrameSample frameSample = (FrameSample) ois.readObject();
		        objectOut.writeObject(frameSample.frame);
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        try {
			objectOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	


}
