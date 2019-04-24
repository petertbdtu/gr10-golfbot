package golfbot.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

import golfbot.server.communication.LidarReceiver;
import golfbot.server.communication.CommandTransmitter;
import golfbot.server.communication.LegoReceiver;

public class RemoteMain {

	public static void main(String[] args) throws IOException {
		// Very important boolean
		boolean YesRobotRunYesYes = true;
		
		// Build Lidar receiver
		LidarReceiver lidarReceiver = new LidarReceiver();
		if(YesRobotRunYesYes && lidarReceiver.bindSocket(5000))
			lidarReceiver.run();
		else
			YesRobotRunYesYes = false;
		
		// Build Lego Receiver
		LegoReceiver legoReceiver = new LegoReceiver();
		if(YesRobotRunYesYes && legoReceiver.connect(3000, 3001, 3002))
			legoReceiver.run();
		else
			YesRobotRunYesYes = false;
		
		// Command Transmitter
		CommandTransmitter commandTransmitter = new CommandTransmitter();
		if(YesRobotRunYesYes && commandTransmitter.connect(3003))
			/* nothing? */;
		else
			YesRobotRunYesYes = false;

		while(true) {
			Scanner scan = new Scanner(System.in);
			String hej = scan.next();
			if(hej.equals("f")) { commandTransmitter.robotTravel(0, 1000); }
			if(hej.equals("t+")) { commandTransmitter.robotTravel(90, 0); }
			if(hej.equals("t-")) { commandTransmitter.robotTravel(-90, 0); }
			if(hej.equals("values")) { 
				legoReceiver.switchGetter();
				System.out.println("IsMoving: " + legoReceiver.getIsMoving().toString());
				System.out.println("IsCollecting: " + legoReceiver.getIsCollecting().toString());
				System.out.println("Pose: " + legoReceiver.getPose().toString());
				System.out.print("Årets Scan: ");
				HashMap<Double,Double> scanning = lidarReceiver.getScan();
				for(Double angle : scanning.keySet()) {
					System.out.print("[" + angle + "," + scanning.get(angle) + "] ");	
				}
				System.out.println("");
			}
		}
			
	}
}
