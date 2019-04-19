package golfbot.robot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import golfbot.robot.knowledgesources.KSGyro;
import golfbot.robot.knowledgesources.KSIR;
import golfbot.robot.knowledgesources.KSMotor;
import golfbot.robot.knowledgesources.KSNavigation;
import golfbot.robot.knowledgesources.KSSonic;
import golfbot.robot.knowledgesources.KSTouch;
import golfbot.robot.navigation.GyroPoseProvider;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class RobotMain {

	private static final String IP = "192.168.0.101";
	private static int port = 3000;
	
	private static Socket socket = null;
	private static ObjectInputStream ois = null;
	
	private static KSIR ksIR;
	private static KSGyro ksGyro;
	private static KSSonic ksSonic;
	private static KSTouch ksTouch;
	private static KSMotor ksDrivingMotors;
	private static KSNavigation navigation = new KSNavigation();
	//private static GyroPoseProvider provider = new GyroPoseProvider(navigation.getInstance());
	
	public static void main(String[] args) {
		//initReceiver();
		//initKnowledgeSources();	
		//mainLoop();
		navigation.travelTo(90, 1000);
		System.out.println("x: " + provider.getPose().getX() + " y: " + provider.getPose().getY() + " heading: " + provider.getPose().getHeading());
		navigation.travelTo(33, 777);
		System.out.println("x: " + provider.getPose().getX() + " y: " + provider.getPose().getY() + " heading: " + provider.getPose().getHeading());
		navigation.travelTo(-33, 2000);
		System.out.println("x: " + provider.getPose().getX() + " y: " + provider.getPose().getY() + " heading: " + provider.getPose().getHeading());
		navigation.travelTo(88, 3000);
		System.out.println("x: " + provider.getPose().getX() + " y: " + provider.getPose().getY() + " heading: " + provider.getPose().getHeading());
		//closeConnections();	
	}
	
	public static void initReceiver() {
		try {
			socket = new Socket(IP,port++);
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Main Receiver : Couldn't connect");
		}
	}
	
	public static void initKnowledgeSources() {
		//Init sensors
		ksIR = new KSIR(SensorPort.S1);
		if(!ksIR.connect(IP, port++)) {
			System.out.println("IR : Couldn't connect");
		}
		
		ksGyro = new KSGyro(SensorPort.S2);
		if(!ksGyro.connect(IP, port++)) {
			System.out.println("Gyro : Couldn't connect");
		}
		
		ksSonic = new KSSonic(SensorPort.S3);
		if(!ksSonic.connect(IP, port++)) {
			System.out.println("Sonic : Couldn't connect");
		}
		
		ksTouch = new KSTouch(SensorPort.S4);
		if(!ksTouch.connect(IP, port++)) {
			System.out.println("Touch : Couldn't connect");
		}
		
		//Init Motors
		ksDrivingMotors = new KSMotor(MotorPort.A,MotorPort.B);
		if(!ksDrivingMotors.connect(IP, port++)) {
			System.out.println("Motor Left : Couldn't connect");
		}
	}
	
	public static void mainLoop() {
		while(!socket.isClosed() && socket.isConnected()) {
			String msg = null;
			try { msg = (String) ois.readObject(); } 
			catch (ClassNotFoundException | IOException e) { break;	}
			if(msg != null) {
				System.out.println("Recieved: " + msg);
			}
		}
	}
	
	public static void closeConnections() {
		try { ois.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { socket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		ksGyro.closeConnection();
		ksIR.closeConnection();
		ksDrivingMotors.closeConnection();
		ksSonic.closeConnection();
		ksTouch.closeConnection();
	}
}


