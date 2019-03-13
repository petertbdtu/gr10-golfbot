package golfbot.navigation;

import lejos.robotics.chassis.Chassis;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveController;
import lejos.robotics.navigation.MoveListener;

/**
 * Class to navigate the robot based on the Lejos Waypoint object
 * @author Frederik
 *
 */
public class NavigationPilot implements MoveController {
	
	private Chassis _chassis;
	
	public NavigationPilot(Chassis chassis) {
		_chassis = chassis;
	}

	@Override
	public Move getMovement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addMoveListener(MoveListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forward() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void backward() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isMoving() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void travel(double distance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void travel(double distance, boolean immediateReturn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLinearSpeed(double speed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getLinearSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxLinearSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLinearAcceleration(double acceleration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getLinearAcceleration() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
