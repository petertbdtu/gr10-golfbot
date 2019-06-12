package robot;

import Knowledgesource.KSBallManagement;
import Knowledgesource.KSNavigation;
import lejos.hardware.port.MotorPort;

public class RobotMain {
	public static void main(String [ ] args) {
		// Server IP hello
		String ip = "172.20.10.2";
		int portSend = 3000;
		int portReceive = 3001;
		
		// Really important bool
		boolean YesRobotRunYesYes = true;
		
		// Build Navigation
		KSNavigation navigation = new KSNavigation();
		if(YesRobotRunYesYes)
			YesRobotRunYesYes = navigation.connect(ip, portSend);
	
		// Build ball management
		KSBallManagement ballManager = new KSBallManagement(MotorPort.C, MotorPort.D);
		if(YesRobotRunYesYes)
			YesRobotRunYesYes = ballManager.connect(ip, portSend);

		// Command Receiver
		CommandReceiver receiver = new CommandReceiver(navigation, ballManager);
		if(YesRobotRunYesYes && receiver.connect(ip, portReceive))
			receiver.start();
		else
			YesRobotRunYesYes = false;
		
		// Wait until receiver is dead
		if(YesRobotRunYesYes) {
			try { receiver.join(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}
