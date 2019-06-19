package Knowledgesource;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.*;
import lejos.utility.Delay;


public class KSBallManagement extends KnowledgeSource {

	EV3MediumRegulatedMotor tubeMotor;
	EV3MediumRegulatedMotor scoopMotor;
	
	public KSBallManagement(Port tubeMotorPort, Port scoopMotorPort) {
		this.tubeMotor = new EV3MediumRegulatedMotor(tubeMotorPort);
		this.scoopMotor = new EV3MediumRegulatedMotor(scoopMotorPort);
		this.tubeMotor.setSpeed(360);;
	}
	
	public void pickup() {
		tubeMotor.rotate(400);
		scoopMotor.rotate(360);
		tubeMotor.rotate(-400);
		//scoopMotor.rotate(20);
		//tubeMotor.rotate(-200);
		//scoopMotor.rotate(20);
		//tubeMotor.rotate(-400);
		
		//tubeMotor.backward();
		//while(!tubeMotor.isStalled());
	}
	
	public void deliverBalls() {
		scoopMotor.rotate(-45);
		Delay.msDelay(2000);
		scoopMotor.rotate(45);
	}

	@Override
	protected byte[] getKnowledgeAsBytes() {
		return new byte[] {0};
	}
	
}
