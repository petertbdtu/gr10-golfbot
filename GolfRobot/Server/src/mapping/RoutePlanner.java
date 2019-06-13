package mapping;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import objects.Point;
import objects.Pose;


public class RoutePlanner {
	
	int ncords = 5;

	int xcords[] = new int[ncords]; 
	int ycords[] = new int[ncords];

	private final int ROBOT_WIDTH = 160;
	private final int ROBOT_FRONT_LENGTH = 160;
	private final int ROBOT_BACK_LENGTH = 80;
	private final int ROBOT_FRONT_TIP = 200;

	List<objects.Point> listArea = new ArrayList<objects.Point>();

	Polygon collisionHull;
	
	AffineTransform rotationTransform;
	
	public RoutePlanner(List<objects.Point> points, Point p) {
		listArea = points;
		buildCoordsFromPoint(p);
		collisionHull = new Polygon(xcords,ycords,ncords);
		rotationTransform = new AffineTransform();
		rotationTransform.rotate(Math.toRadians(10),collisionHull.xpoints[0],collisionHull.ypoints[0]);
	}
	
	/*
	private float checkInterval = 1;
	private float robotSize = 1; // It is probably not 1.
	private float rotationInterval = 90;
	private double destPrecision = 0.1;
	private double headingPrecision = 1;
		
	public RoutePlanner(float checkInterval, float robotSize, float rotationInterval, double destPrecision, double headingPrecision) {
		this.checkInterval = checkInterval;
		this.robotSize = robotSize;
		this.rotationInterval = rotationInterval;
		this.destPrecision = destPrecision;
		this.headingPrecision = headingPrecision;
	}
	
	public List<Pose> plan(Point start, Point goal) {
		List<Pose> route = initRouteTwoWay(start, goal);
		System.out.println("Initial route length: "+route.size());
		
		route = cleanupSameHeading(route);
		System.out.println("Route length after same-heading cleanup: "+route.size());
		
		route = flowBackwards(route);
		System.out.println("Route length after backflow cleanup: "+route.size());
		
		return route;
	}
	
	private ArrayList<Pose> initRouteTwoWay(Point start, Point goal) {
		ArrayList<Point> forwards = new ArrayList<>();
		ArrayList<Point> reverse = new ArrayList<>();
		// TODO find out if it is necessary to add the goal point to the reverse list.
		
		Point nextf = nextStep(start, goal);
		Point nextr = nextStep(goal, start);
		int maxIter = 1000;
		int iterations = 0;
		// Stop once goal reached in either path.
		// Or once iterated too many times.
		while (iterations<=maxIter && !withinSquare(goal, nextf) && !withinSquare(start, nextr)) {
			nextf = nextStep(nextf, goal);
			forwards.add(nextf);
			nextr = nextStep(nextr, start);
			reverse.add(nextr);
			
			iterations++;
		}
		
		if (withinSquare(goal, nextf))
		{
			System.out.println("forwards");
			forwards.add(new Point(goal));
			return forwards;
		}
		System.out.println("reverse");
		Collections.reverse(reverse);
		reverse.add(new Point(goal));
		return reverse;
	}
	
	private Point nextStep(Point start, Point goal) {
		Pose s = new Pose(start.x, start.y, 0);
		
		// Face target
		s.rotateUpdate(s.relativeBearing(goal));
		// Move towards target, interval or less
		if (s.distanceTo(goal) > checkInterval)
			s.moveUpdate(checkInterval);
		else
			s.moveUpdate(s.distanceTo(goal));
		
		float heading = s.getHeading();
		while (og.checkCollisions(s.getLocation(), robotSize)) {
			heading += rotationInterval;
			s = new Pose(start.x, start.y, heading);
			s.moveUpdate(checkInterval);
		}
		return new Pose(s);
	}
	
	private List<Pose> cleanupSameHeading(List<Pose> route) {
		ArrayList<Pose> cleanRoute = new ArrayList<>();
		
		Pose lastWaypoint = new Pose(0,0,-10000);
		for (Pose wp : route) {
			if (!sameHeading(lastWaypoint, wp))
				cleanRoute.add(wp);
			lastWaypoint = wp;
		}
		
		return cleanRoute;
	}
	
	private List<Point> flowBackwards(List<Point> route) {
		ArrayList<Point> newRoute = new ArrayList<>();
		
		/*
		 * Tries to find longest shortcuts by going backwards from the end.
		 * Every time it finds a longest shortcut, it finds shortcuts
		 * from then on. Might not actually be optimal, but it works alright.
		 */
		/*
		int i = 0;
		int j = route.size()-1;
		while (i < j) {
			if (canDriveStraight(route.get(i), route.get(j))) {
				newRoute.add(route.get(j));
				i = j;
				j = route.size()-1;
			}
			else {
				j--;
			}
		}
		return newRoute;
	}
	
	// TODO Nicolai, please give me an OccupancyGrid function that does this.
	private boolean canDriveStraight(Point a, Point b) {
		Pose s = new Pose(a.x, a.y, 0);
		s.rotateUpdate(s.relativeBearing(b));
		
		while (!withinSquare(s.getLocation(), b))
		{
			if (og.checkCollisions(s.getLocation(), robotSize)) {
				return false;				
			}
			if (s.distanceTo(b) > checkInterval)
				s.moveUpdate(checkInterval);
			else
				s.moveUpdate(s.distanceTo(b));
		}
				
		return true;
	}

	private boolean sameHeading(Pose a, Pose b) {
		return a.getHeading()-headingPrecision <= b.getHeading() && b.getHeading() <= a.getHeading()+headingPrecision;
	}
	
	private boolean withinSquare(Point squareCenter, Point b) {
		return squareCenter.x-destPrecision <= b.x && b.x <= squareCenter.x+destPrecision &&
				squareCenter.y-destPrecision <= b.y && b.y <= squareCenter.y+destPrecision;
	}*/
	
	private objects.Pose findValidPosition(){
		for(int i = 0; i<36; i++){
			boolean collisionDetected = false;
			for(objects.Point p : listArea){
				if(collisionHull.contains(p)){
					collisionDetected = true;
					break;
				}
			}
			if(collisionDetected){
				collisionHull = (Polygon) rotationTransform.createTransformedShape(collisionHull);
				continue;
			}	

			Rectangle tmp = collisionHull.getBounds();

			return new objects.Pose(tmp.x + tmp.width/2,tmp.y + tmp.height/2,angleOf(new Point((collisionHull.xpoints[2] + collisionHull.xpoints[3])/2,(collisionHull.ypoints[2] + collisionHull.ypoints[3])/2),new Point(collisionHull.xpoints[0],collisionHull.ypoints[0])));

		}

		return null;
	}
	
	private float angleOf(Point p1, Point p2) {
	    double deltaY = (p1.y - p2.y);
	    double deltaX = (p2.x - p1.x);
	    double result = Math.toDegrees(Math.atan2(deltaY, deltaX)); 
	    return (float) ((result < 0) ? (360d + result) : result);
	}
	
	private void buildCoordsFromPoint(Point p){
		  xcords[0] = p.x;
		  ycords[0] = p.y;

		  xcords[1] = p.x - (ROBOT_WIDTH/2);
		  ycords[1] = p.y - (ROBOT_FRONT_TIP-ROBOT_FRONT_LENGTH);

		  xcords[2] = p.x - (ROBOT_WIDTH/2);
		  ycords[2] = p.y - (ROBOT_FRONT_TIP+ROBOT_BACK_LENGTH);

		  xcords[3] = p.x + (ROBOT_WIDTH/2);
		  ycords[3] = p.y - (ROBOT_FRONT_TIP+ROBOT_BACK_LENGTH);

		  xcords[4] = p.x + (ROBOT_WIDTH/2);
		  ycords[4] = p.y - (ROBOT_FRONT_TIP-ROBOT_FRONT_LENGTH);

		}
	
	public objects.Pose findBestPosition(List<objects.Point> points, Point p) {
		listArea = points;
		
		buildCoordsFromPoint(p);
		collisionHull = new Polygon(xcords,ycords,ncords);
		
		rotationTransform = new AffineTransform();
		rotationTransform.rotate(Math.toRadians(10),collisionHull.xpoints[0],collisionHull.ypoints[0]);
		
		objects.Pose tmp = findValidPosition();
		if(tmp != null) {
			tmp.moveUpdate(-100);
		}
		return tmp;
	}
}