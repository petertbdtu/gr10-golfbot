package golfbot.robot;

public class KSTest extends KnowledgeSource<String> {

	private String msg;
	
	public KSTest(String msg) {
		this.msg = msg;
	}
	
	@Override
	protected String getKnowledge() {
		try {
			this.wait(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String msg = "Hi : " + System.currentTimeMillis();
		return msg;
	}

	
}
