package golfbot.robot;

import java.io.IOException;

import golfbot.robot.knowledgesources.KSCamera;
import lejos.utility.Delay;

public class TestKamera {
	
	private static final String IP = "172.20.10.2";
	private static int port = 3000;
	
	public static void main(String[] Args) {
		KSCamera ksCamera = null;
		try { ksCamera = new KSCamera(); } 
		catch (IOException e) { e.printStackTrace(); }
		if(!ksCamera.connect(IP, port++)) {
			System.out.println("IR : Couldn't connect");
		}
		
		Delay.msDelay(10000);
	}
}
