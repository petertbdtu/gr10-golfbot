package Knowledgesource;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.*;


public class KSBallManagement extends KnowledgeSource {

	EV3MediumRegulatedMotor motor;
	
	public KSBallManagement() {
		motor = new EV3MediumRegulatedMotor(MotorPort.C);
	}
	
	public void pickup() {
		while(!motor.isStalled()) {
			motor.forward();
		}
		motor.flt();
		while(!motor.isStalled()) {
			motor.backward();
		}
	}

	@Override
	protected byte[] getKnowledgeAsBytes() {
		return new byte[] {0};
	}
	
}
