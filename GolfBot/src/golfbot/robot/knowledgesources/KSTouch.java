package golfbot.robot.knowledgesources;

import golfbot.samples.TouchSample;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3TouchSensor;

public class KSTouch extends KnowledgeSource<TouchSample> {

	private EV3TouchSensor touchSensor;
	
	public KSTouch(Port port) {
		this.touchSensor = new EV3TouchSensor(port);
		this.touchSensor.getTouchMode();
	}
	
	@SuppressWarnings("unused")
	@Override
	protected TouchSample getKnowledge() {
		float[] sample = null;
		TouchSample ts = null;
		touchSensor.fetchSample(sample, 0);

		if(sample != null && sample.length > 0)
			ts = new TouchSample(sample[0]);
		
		return ts;
	}	
}