package golfbot.robot.knowledgesources;

import java.io.IOException;

import golfbot.samples.FrameSample;
import lejos.hardware.BrickFinder;
import lejos.hardware.video.Video;

public class KSCamera extends KnowledgeSource<FrameSample> {

	private Video video;
	
	public KSCamera() throws IOException {
		this.video = BrickFinder.getLocal().getVideo();
		video.open(1920, 1080);
	}
	
	@SuppressWarnings("unused")
	@Override
	protected FrameSample getKnowledge() {
		return new FrameSample(video.createFrame());
	}	
}