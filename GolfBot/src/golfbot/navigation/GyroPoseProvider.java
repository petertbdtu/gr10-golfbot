package golfbot.navigation;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;

public class GyroPoseProvider implements PoseProvider, MoveListener {
	public boolean state;
	private float oldAngle;
	private float oldDistance;
	private float direction = 0;
	private float x = 0;
	private float y = 0;
	MoveProvider mp;
	EV3GyroSensor gyro = new EV3GyroSensor(SensorPort.S2);
	
	public GyroPoseProvider(MoveProvider mp) {
		mp.addMoveListener(this);
	}
	
	
	
	public synchronized float getGyroAngle() {
		SampleProvider sampleProvider = gyro.getAngleMode();
		float[] sample = new float[sampleProvider.sampleSize()];
		sampleProvider.fetchSample(sample, 0);
		return sample[0];
	}
	
	public synchronized float getGyroAngleAndRate() {
		SampleProvider sampleProvider = gyro.getAngleAndRateMode();
		float[] sample = new float[sampleProvider.sampleSize()];
		sampleProvider.fetchSample(sample, 0);
		return sample[0];
	}
	public void setPos(Point p) {
		x = p.x;
		y = p.y;
		state = true;
	}
	
	public void setDirection(float direction) {
		this.direction = direction;
		state = true;
	}
	
	

	@Override
	public void moveStarted(Move event, MoveProvider mp) {
		this.mp = mp;
		oldDistance = 0;
		oldAngle = 0;
		state = false;
	}

	@Override
	public void moveStopped(Move event, MoveProvider mp) {
		update(event);
	}

	@Override
	public Pose getPose() {
		if(state != true) {
			update(mp.getMovement());
		}
		return new Pose(x, y, direction);
	}


	private void update(Move movement) {
		float newAngle = getGyroAngle() - oldAngle;
		gyro.reset();
		float newDistance = movement.getDistanceTraveled() - oldDistance;
		float radians = (float) Math.toRadians(direction);
		float xDistance = 0;
		float yDistance = 0;
		
		if(movement.getMoveType() == Move.MoveType.TRAVEL) {
			xDistance = newDistance * (float) Math.cos(radians);
			yDistance = newDistance * (float) Math.sin(radians);
		} else if (movement.getMoveType() == Move.MoveType.ARC) {
			//Here for if needed to be implemented
			// Use getGyroAngleAndRate() to get angle and rate
			System.out.println("Not able to use ARC mode for localization yet");
		}
		x += xDistance;
		y += yDistance;
		oldAngle = newAngle;
		oldDistance = movement.getDistanceTraveled();
		if(direction + newAngle > 180) {
			direction += newAngle -360;
		} else if (direction + newAngle < -180) {
			direction += newAngle + 360;
		} else {
			direction += newAngle;
		}
		state = !movement.isMoving();
	}

	@Override
	public void setPose(Pose aPose) {
		setPos(aPose.getLocation());
		setDirection(aPose.getHeading());
	}

}
