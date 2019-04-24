package golfbot.server;

import java.io.IOException;
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
		System.out.println("Build Lidar Receiver - robot running? " + YesRobotRunYesYes);
		LidarReceiver lidarReceiver = new LidarReceiver();
		if(YesRobotRunYesYes && lidarReceiver.bindSocket(5000))
			lidarReceiver.start();
		else
			YesRobotRunYesYes = false;
		
		// Build Lego Receiver
		System.out.println("Build Lego Receiver - robot running? " + YesRobotRunYesYes);
		LegoReceiver legoReceiver = new LegoReceiver();
		if(YesRobotRunYesYes && legoReceiver.connect(3000, 3001, 3002))
			legoReceiver.start();
		else
			YesRobotRunYesYes = false;
		
		// Command Transmitter
		System.out.println("Build Command Transmitter - robot running? " + YesRobotRunYesYes);
		CommandTransmitter commandTransmitter = new CommandTransmitter();
		if(YesRobotRunYesYes)
			YesRobotRunYesYes = commandTransmitter.connect(3003);

		while(YesRobotRunYesYes) {
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
