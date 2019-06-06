package mapping;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//
//public class RoutePlanner {
//	
//	private float checkInterval = 1;
//	private float robotSize = 1; // It is probably not 1.
//	private float rotationInterval = 90;
//	private BLOccupancyGrid og;
//	private double destPrecision = 0.1;
//	private double headingPrecision = 1;
//		
//	public RoutePlanner(BLOccupancyGrid occupancyGrid, float checkInterval, float robotSize, float rotationInterval, double destPrecision, double headingPrecision) {
//		this.og = occupancyGrid;
//		this.checkInterval = checkInterval;
//		this.robotSize = robotSize;
//		this.rotationInterval = rotationInterval;
//		this.destPrecision = destPrecision;
//		this.headingPrecision = headingPrecision;
//	}
//	
//	public List<Waypoint> plan(Point start, Point goal) {
//		List<Waypoint> route = initRouteTwoWay(start, goal);
//		System.out.println("Initial route length: "+route.size());
//		
//		route = cleanupSameHeading(route);
//		System.out.println("Route length after same-heading cleanup: "+route.size());
//		
//		route = flowBackwards(route);
//		System.out.println("Route length after backflow cleanup: "+route.size());
//		
//		return route;
//	}
//	
//	private List<Waypoint> initRouteTwoWay(Point start, Point goal) {
//		ArrayList<Waypoint> forwards = new ArrayList<>();
//		ArrayList<Waypoint> reverse = new ArrayList<>();
//		// TODO find out if it is necessary to add the goal point to the reverse list.
//		
//		Waypoint nextf = nextStep(start, goal);
//		Waypoint nextr = nextStep(goal, start);
//		int maxIter = 1000;
//		int iterations = 0;
//		// Stop once goal reached in either path.
//		// Or once iterated too many times.
//		while (iterations<=maxIter && !withinSquare(goal, nextf) && !withinSquare(start, nextr)) {
//			nextf = nextStep(nextf, goal);
//			forwards.add(nextf);
//			nextr = nextStep(nextr, start);
//			reverse.add(nextr);
//			
//			iterations++;
//		}
//		
//		if (withinSquare(goal, nextf))
//		{
//			System.out.println("forwards");
//			forwards.add(new Waypoint(goal));
//			return forwards;
//		}
//		System.out.println("reverse");
//		Collections.reverse(reverse);
//		reverse.add(new Waypoint(goal));
//		return reverse;
//	}
//	
//	private Waypoint nextStep(Point start, Point goal) {
//		Pose s = new Pose(start.x, start.y, 0);
//		
//		// Face target
//		s.rotateUpdate(s.relativeBearing(goal));
//		// Move towards target, interval or less
//		if (s.distanceTo(goal) > checkInterval)
//			s.moveUpdate(checkInterval);
//		else
//			s.moveUpdate(s.distanceTo(goal));
//		
//		float heading = s.getHeading();
//		while (og.checkCollisions(s.getLocation(), robotSize)) {
//			heading += rotationInterval;
//			s = new Pose(start.x, start.y, heading);
//			s.moveUpdate(checkInterval);
//		}
//		return new Waypoint(s);
//	}
//	
//	private List<Waypoint> cleanupSameHeading(List<Waypoint> route) {
//		ArrayList<Waypoint> cleanRoute = new ArrayList<>();
//		
//		Waypoint lastWaypoint = new Waypoint(0,0,-10000);
//		for (Waypoint wp : route) {
//			if (!sameHeading(lastWaypoint, wp))
//				cleanRoute.add(wp);
//			lastWaypoint = wp;
//		}
//		
//		return cleanRoute;
//	}
//	
//	private List<Waypoint> flowBackwards(List<Waypoint> route) {
//		ArrayList<Waypoint> newRoute = new ArrayList<>();
//		
//		/*
//		 * Tries to find longest shortcuts by going backwards from the end.
//		 * Every time it finds a longest shortcut, it finds shortcuts
//		 * from then on. Might not actually be optimal, but it works alright.
//		 */
//		
//		int i = 0;
//		int j = route.size()-1;
//		while (i < j) {
//			if (canDriveStraight(route.get(i), route.get(j))) {
//				newRoute.add(route.get(j));
//				i = j;
//				j = route.size()-1;
//			}
//			else {
//				j--;
//			}
//		}
//		return newRoute;
//	}
//	
//	// TODO Nicolai, please give me an OccupancyGrid function that does this.
//	private boolean canDriveStraight(Point a, Point b) {
//		Pose s = new Pose(a.x, a.y, 0);
//		s.rotateUpdate(s.relativeBearing(b));
//		
//		while (!withinSquare(s.getLocation(), b))
//		{
//			if (og.checkCollisions(s.getLocation(), robotSize)) {
//				return false;				
//			}
//			if (s.distanceTo(b) > checkInterval)
//				s.moveUpdate(checkInterval);
//			else
//				s.moveUpdate(s.distanceTo(b));
//		}
//		
//		return true;
//	}
//
//	private boolean sameHeading(Waypoint a, Waypoint b) {
//		return a.getHeading()-headingPrecision <= b.getHeading() && b.getHeading() <= a.getHeading()+headingPrecision;
//	}
//	
//	private boolean withinSquare(Point squareCenter, Point p) {
//		return squareCenter.x-destPrecision <= p.x && p.x <= squareCenter.x+destPrecision &&
//				squareCenter.y-destPrecision <= p.y && p.y <= squareCenter.y+destPrecision;
//	}
//}