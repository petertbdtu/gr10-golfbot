

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Scanner;

import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import blackboard.BLBallDetector;
import blackboard.BlackboardController;
import blackboard.BlackboardSample;
import communication.CommandTransmitter;
import communication.LegoReceiver;
import communication.LidarReceiver;
import mapping.LidarScan;
import objects.LidarSample;

public class RemoteMain {

	public static void main(String[] args) throws IOException {
		// Very important boolean
		boolean YesRobotRunYesYes = true;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Build Lidar receiver
		System.out.println("Building Lidar Receiver...");
		LidarReceiver lidarReceiver = new LidarReceiver();
		if(YesRobotRunYesYes && lidarReceiver.bindSocket(5000)) {
			lidarReceiver.start();
			System.out.println("Lidar Receiver succes");
		} else {
			YesRobotRunYesYes = false;
			System.out.println("Lidar Receiver failed");
		}
			
//		// Build Lego Receiver
//		System.out.println("Building Lego Receiver...");
//		LegoReceiver legoReceiver = new LegoReceiver();
//		if(YesRobotRunYesYes && legoReceiver.connect(3000)) {
//			legoReceiver.start();
//			System.out.println("Lego Receiver succes");
//		} else {
//			YesRobotRunYesYes = false;
//			System.out.println("Lego Receiver failed");
//		}
//		
//		// Command Transmitter
//		System.out.println("Building Command Transmitter...");
//		CommandTransmitter commandTransmitter = new CommandTransmitter();
//		if(YesRobotRunYesYes) {
//			YesRobotRunYesYes = commandTransmitter.connect(3001);
//			System.out.println("Command Transmitter succes");
//		} else {
//			YesRobotRunYesYes = false;
//			System.out.println("Command Transmitter failed");
//		}
//		
//		// Blackboard Controller
//		System.out.println("Building blackboard...");
//		BlackboardController bController = new BlackboardController(null, legoReceiver, null);
//		bController.registerListener(commandTransmitter);
//		if(YesRobotRunYesYes) {
//			bController.start();
//			System.out.println("Blackboard succes");
//		} else {
//			System.out.println("Blackboard not started");
//		}
			
		//Remove main
		System.out.println("Start Manual Remote-Control...");
		Scanner scan = new Scanner(System.in);
		BLBallDetector ballDetector = new BLBallDetector();
		
		while(YesRobotRunYesYes) {
			printMenu();
			System.out.println();
			System.out.println();
			switch (scan.next()) {
//				case "1" : {
//					System.out.println("How far?");
//					commandTransmitter.robotTravel(0, scan.nextInt()); 
//					break;
//				}
//				case "2" : {
//					System.out.println("How far?");
//					commandTransmitter.robotTravel(0, -scan.nextInt());
//					break;
//				}
//				case "3" : {
//					System.out.println("How much?");
//					commandTransmitter.robotTravel(scan.nextInt(), 0);
//					break;
//				}
//				case "4" : {
//					System.out.println("How much?");
//					commandTransmitter.robotTravel(-scan.nextInt(), 0);
//					break;
//				}
//				case "5" : {
//					// not implemented
//					break;
//				}
//				case "6" : {
//					BlackboardSample bSample = commandTransmitter.getSample();
//					if(bSample != null) {
//						System.out.println(">>> DATA <<<");
//						System.out.println("IsMoving: " + bSample.isMoving);
//						System.out.println("IsCollecting: " + bSample.isCollecting);
//						System.out.println("Pose: " + bSample.robotPose.toString());
//						System.out.print("TheScan: ");
//						LidarScan lScan = bSample.scan;
//						for(LidarSample sample : lScan.getSamples()) {
//							System.out.print("[" + sample.angle + "," + sample.distance + "] ");	
//						}
//					}
//
//					break;
//				}
				case "7" : {
					LidarScan lScan = lidarReceiver.getScan();
					if(lScan != null) {
						try {
							for(LidarSample sample : lScan.getSamples()) {
								System.out.println("[" + sample.angle + "," + sample.distance + "] ");	
							}
							Imgcodecs.imwrite("testScan2.png", ballDetector.getMap(lScan));
				            FileOutputStream fileOut = new FileOutputStream("testScan2.data");
				            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
				            objectOut.writeObject(lScan);
				            objectOut.close();
				            System.out.println("The Object was succesfully written to testScan.data");
				 
				        } catch (Exception ex) {
				            System.out.println("The Object could not be written to a file");
				        }
					}
					break;
				}
				default : {
					System.out.println("not valid");
					break;
				}
			}
		}
				
		//Finish
		System.out.println("Shutting down...");
		scan.close();
	}
	
	private static void printMenu() {
		System.out.println();
		System.out.println(">>> MENU <<<");
		System.out.println("  1: drive forward");
		System.out.println("  2: drive backwards");
		System.out.println("  3: turn right");
		System.out.println("  4: turn left");
		System.out.println("  5: pickup ball");
		System.out.println("  6: print values");
		System.out.println("  7: save scan");
		System.out.println("  8: save scan as image");
		System.out.println();
	}
}
