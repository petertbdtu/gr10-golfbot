package objects;

public class Point extends java.awt.Point {

	public Point(int x, int y) {
		super(x,y);
	}
	
	public Point(Point point) {
		super(point);
	}

    public Point pointAt(double distance, double angle) {
      int x = (int) (distance*Math.cos(Math.toRadians(angle)) + this.x);
      int y = (int) (distance*Math.sin(Math.toRadians(angle)) + this.y);
      return new Point(x,y);
    }
    
    public float angleTo(Point p)
    {
      return (float)Math.toDegrees(Math.atan2(p.getY()-y,  p.getX()-x));
    }
    
    public void translate(float x, float y) {
    	this.x += x;
    	this.y += y;
    }
    
    public String toString() {
    	return "X: " + x + "  Y: " + y;
    }
}
