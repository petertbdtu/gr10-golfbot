package Knowledgesource;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.*;


public class KSBallManagement extends KnowledgeSource {

	EV3MediumRegulatedMotor motor;
	EV3MediumRegulatedMotor motor2;
	
	public KSBallManagement() {
		motor = new EV3MediumRegulatedMotor(MotorPort.C);
		motor2 = new EV3MediumRegulatedMotor(MotorPort.D);
	}
	
	public void pickup() {
		int tacho = 0;
		while(!motor.isStalled()) {
			motor.forward();
		}
		motor.flt();
		motor2.rotate(360);
		while(!motor.isStalled()) {
			motor.backward();
		}
	}

	@Override
	protected byte[] getKnowledgeAsBytes() {
		return new byte[] {0};
	}
	
}
