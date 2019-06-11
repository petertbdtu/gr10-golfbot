package objects;

public class Pose {

	public Point point;
	public float heading;
	
	public Pose(int x, int y, float heading) {
		this.point = new Point(x,y);
		this.heading = heading;
	}
	
	public Pose() {
		this.point = new Point(0, 0);	
		this.heading = 0;
	}
	public Pose(Pose pose) {
		this.point.x = pose.point.x;
		this.point.y = pose.point.y;
		this.heading = pose.heading;
	}
	
	public float distanceTo(Point point) {
		return (float) point.distance(point);
	}
	
	public void moveUpdate(float distance)
	{
	  float x = distance * (float)Math.cos(Math.toRadians(heading));
	  float y = distance * (float)Math.sin(Math.toRadians(heading));
	  point.translate(x,y);
	}
	
	public void rotateUpdate(float angle)
	{
	  heading += angle;
	  while(heading < 180) 
		  heading += 360;
	  while(heading > 180) 
		  heading -= 360;
	}
	
	public float relativeBearing(Point destination)
	{
	  float bearing = angleTo(destination) - heading;
	  if(bearing < -180)bearing +=360;
	  if(bearing > 180)bearing -= 360;
	  return bearing;
	}
	
	public float angleTo(Point destination)
	{
	  return point.angleTo(destination);
	}
	
	public float getHeading() {
		return heading;
	}
	
	public Point getLocation() {
		return point;
	}
	
	public boolean equals(Pose pose) {
		return (this.point.x == pose.point.x && this.point.y == pose.point.y && this.heading == pose.heading);
	}
	
	public String toString() {
		return "[" + this.point.x + "," + this.point.y + "," + this.heading + "]";
	}
}
