package golfbot.robot.knowledgesources;

import golfbot.sharedObjects.SonicSample;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class KSSonic extends KnowledgeSource<SonicSample> {

	private EV3UltrasonicSensor sonicSensor;
	
	public KSSonic(Port port) {
		this.sonicSensor = new EV3UltrasonicSensor(port);
		this.sonicSensor.getDistanceMode();
	}
	
	@SuppressWarnings("unused")
	@Override
	protected SonicSample getKnowledge() {
		float[] sample = null;
		SonicSample ss = null;
		sonicSensor.fetchSample(sample, 0);

		if(sample != null && sample.length > 0)
			ss = new SonicSample(sample[0]);
		
		return ss;
	}
}