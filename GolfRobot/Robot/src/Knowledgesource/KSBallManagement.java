package Knowledgesource;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.*;


public class KSBallManagement extends KnowledgeSource {

	EV3MediumRegulatedMotor tubeMotor;
	EV3MediumRegulatedMotor scoopMotor;
	
	public KSBallManagement(Port tubeMotorPort, Port scoopMotorPort) {
		this.tubeMotor = new EV3MediumRegulatedMotor(tubeMotorPort);
		this.scoopMotor = new EV3MediumRegulatedMotor(scoopMotorPort);
	}
	
	public void pickup() {
		tubeMotor.forward();
		while(!tubeMotor.isStalled());
		tubeMotor.flt();
		scoopMotor.rotate(360);
		tubeMotor.backward();
		while(!tubeMotor.isStalled());
	}

	@Override
	protected byte[] getKnowledgeAsBytes() {
		return new byte[] {0};
	}
	
}
