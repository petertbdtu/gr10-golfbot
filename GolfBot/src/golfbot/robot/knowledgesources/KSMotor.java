package golfbot.robot.knowledgesources;

import golfbot.samples.DrivingMotorSample;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

public class KSMotor extends KnowledgeSource<DrivingMotorSample> {

	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	
	public KSMotor(Port leftMotorPort, Port rightMotorPort) {
		this.leftMotor = new EV3LargeRegulatedMotor(leftMotorPort);
		this.rightMotor = new EV3LargeRegulatedMotor(rightMotorPort);
	}
	
	@Override
	protected DrivingMotorSample getKnowledge() {
		return new DrivingMotorSample(leftMotor.getTachoCount(),rightMotor.getTachoCount());
	}
}