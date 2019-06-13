package Knowledgesource;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class KSNavigation extends KnowledgeSource {

	private volatile boolean isMoving;
	private final double offset = 78; //distance between the two wheels divided by 2 in mm
	private final double wheelDiamater = 30; //Diameter of wheels in mm
	
	private Wheel leftWheel;
	private Wheel rightWheel;
	private Chassis chassis;
	private MovePilot movePilot;
	
	private EV3GyroSensor gyro;
	private SampleProvider sampleProvider;
	
	public KSNavigation() {
		this.leftWheel = WheeledChassis.modelWheel(new EV3LargeRegulatedMotor(MotorPort.A), wheelDiamater).offset(offset);
		this.rightWheel = WheeledChassis.modelWheel(new EV3LargeRegulatedMotor(MotorPort.B), wheelDiamater).offset(-offset);
		this.chassis = new WheeledChassis(new Wheel[] {leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
		this.movePilot = new MovePilot(chassis);
		movePilot.setLinearSpeed(200);
		movePilot.setAngularSpeed(40);
		
		this.gyro = new EV3GyroSensor(SensorPort.S2);
		this.sampleProvider = gyro.getAngleMode();

	}
	
	public void forward(final double distance) {
		isMoving = true;
		//final double distanceSign = distance > 0 ? -distance : distance;
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					movePilot.travel(-distance);
					isMoving = false;
				}
			}
		).start();
	}
	
	public void turn(final double angle) {
		isMoving = true;
		final int angleSign = angle > 0 ? -360 : 360;
		gyro.reset();
		movePilot.rotate(angleSign, true);

		new Thread(
				new Runnable() {
					@Override
					public void run() {
						if(angle > 0) {
							while(getGyroAngle() < angle) {
								LCD.drawString(getGyroAngle() + "", 0, 0);
								Delay.msDelay(200);
							}
						} else {
							while(getGyroAngle() > angle) {
								LCD.drawString(getGyroAngle() + "", 0, 0);
								Delay.msDelay(200);
							}
						}
						movePilot.stop();
						isMoving = false;
					}
				}
		).start();
	}
	
	public void stopMoving() {
		movePilot.stop();
		isMoving = false;
	}

	@Override
	protected byte[] getKnowledgeAsBytes() {
		byte val = isMoving ? (byte) 1 : (byte) 0;
		return new byte[] { val };
	}
	
	private float getGyroAngle() {
		float[] sample = new float[sampleProvider.sampleSize()];
		sampleProvider.fetchSample(sample, 0);
		return sample[0];
	}
	
}
