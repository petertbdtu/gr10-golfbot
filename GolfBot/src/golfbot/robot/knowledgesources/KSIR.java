package golfbot.robot.knowledgesources;

import golfbot.samples.IRSample;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;

public class KSIR extends KnowledgeSource<IRSample> {

	private EV3IRSensor irSensor;
	
	public KSIR(Port port) {
		this.irSensor = new EV3IRSensor(port);
		this.irSensor.getDistanceMode();
	}
	
	@SuppressWarnings("unused")
	@Override
	protected IRSample getKnowledge() {
		float[] sample = null;
		IRSample irs = null;
		irSensor.fetchSample(sample, 0);

		if(sample != null && sample.length > 0)
			irs = new IRSample(sample[0]);
		
		return irs;
	}
}