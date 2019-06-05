package communication;

public class BLBallDetector {
	
    public static void main(String args[])
    {
    	//Point a = new Point(1.5f,1.75f);
    	Point b = new Point(2,2);
    	//Point c = new Point(2.5f,1.75f);
    	//Point center = findCenter(a,b,c);
    	
    	//System.out.println(String.format("Center: x='%f' y='%f'", center.x, center.y));
    }
    
    public static Point findCenter(Point a, Point b, Point c)
    {
        float k1 = (a.y - b.y) / (a.x - b.x); //Two-point slope equation
        float k2 = (a.y - c.y) / (a.x - c.x); //Same for the (A,C) pair
        Point midAB = new Point((a.x + b.x) / 2, (a.y + b.y) / 2); //Midpoint formula
        Point midAC = new Point((a.x + c.x) / 2, (a.y + c.y) / 2); //Same for the (A,C) pair
        k1 = -1*k1; //If two lines are perpendicular, then the product of their slopes is -1.
        k2 = -1*k2; //Same for the other slope
        float n1 = midAB.y - k1*midAB.x; //Determining the n element
        float n2 = midAC.y - k2*midAC.y; //Same for (A,C) pair
        //Solve y1=y2 for y1=k1*x1 + n1 and y2=k2*x2 + n2
        float x = (n2-n1) / (k1-k2);
        float y = k1*x + n1;
        return new Point(0,0); //return new Point(x, y);
    }
    
    public boolean checkForBalls(LidarScan scan) {
  	
    	return false;
    }
    
    public Pose getClosestBall(LidarScan scan) {
    	
    	return null;
    }
    
    public boolean ballAtPose(Pose pose) {
    	
    	return false;
    }
}
