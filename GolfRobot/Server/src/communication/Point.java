package communication;

public class Point extends java.awt.Point {

	public Point(int x, int y) {
		super(x,y);
	}

    public Point pointAt(double distance, double angle) {
      int x = (int) (distance*Math.cos(Math.toRadians(angle)) + this.x);
      int y = (int) (distance*Math.sin(Math.toRadians(angle)) + this.y);
      return new Point(x,y);
    }
}
