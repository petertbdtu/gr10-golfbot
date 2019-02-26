package golfbot.robot.knowledgesources;

import golfbot.sharedObjects.GyroSample;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3GyroSensor;

public class KSGyro extends KnowledgeSource<GyroSample> {

	private EV3GyroSensor sGyro;
	
	public KSGyro(Port port) {
		this.sGyro = new EV3GyroSensor(port);
		sGyro.reset();
		sGyro.getAngleAndRateMode();
	}
	
	@SuppressWarnings("unused")
	@Override
	protected GyroSample getKnowledge() {
		GyroSample gs = null;
		float[] sample = null;		
		sGyro.fetchSample(sample, 0);
		
		if(sample != null && sample.length > 0)
			gs = new GyroSample(sample[0],sample[1]);
		
		return gs;
	}	
}