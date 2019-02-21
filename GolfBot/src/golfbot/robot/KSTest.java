package golfbot.robot;

public class KSTest extends KnowledgeSource<String> {

	private String msg;
	
	public KSTest(String msg) {
		this.msg = msg;
	}
	
	@Override
	protected String getKnowledge() {
		return msg + " : " + System.currentTimeMillis();
	}

	
}
