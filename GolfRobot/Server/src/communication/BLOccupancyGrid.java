package communication;

//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.Serializable;
//import java.util.HashMap;



public class BLOccupancyGrid {

//	private final double OCCUPANCY_WEIGHT = 0.5;
//	private HashMap<Point,OccupancyObject> map;
//	public float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
//	
//	public BLOccupancyGrid() {
//		this.map = new HashMap<Point,OccupancyObject>();
//	}
//	
//	public static BLOccupancyGrid loadTestData(String path) {
//		BLOccupancyGrid og = new BLOccupancyGrid();
//		try {
//			FileInputStream fis = new FileInputStream(path);
//			ObjectInputStream ois = new ObjectInputStream(fis);
//			while(true) {
//				Object obj = ois.readObject();
//				if(obj != null)
//					og.registerOccupancy((Point) obj, true);
//				else
//					break;
//			}
//			ois.close();
//			fis.close();
//		} catch (Exception e) {
//        	e.printStackTrace();
//		}
//		return og;
//	}
//	
//	public static void saveTestData(String path, BLOccupancyGrid og) {
//		try {
//			FileOutputStream fos = new FileOutputStream(path);
//			ObjectOutputStream oos = new ObjectOutputStream(fos);
//			for(Point p : og.map.keySet()) {
//				oos.writeObject(p);
//			}
//			oos.close();
//			fos.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public void registerOccupancy(lejos.robotics.geometry.Point point, boolean isOccupied) {
//		if(point.x > maxX)
//			maxX = point.x;
//		if(point.y > maxY)
//			maxY = point.y;
//		if(point.x < minX)
//			minX = point.x;
//		if(point.y < minY)
//			minY = point.y;
//		
//		if(map.containsKey(point)) {
//			if(isOccupied) { map.put((Point) point, map.get(point).increment()); } 
//			else { map.put((Point) point, map.get(point).decrement()); }
//		} else {
//			if(isOccupied) { map.put((Point) point, new OccupancyObject().increment()); } 
//			else { map.put((Point) point, new OccupancyObject().decrement()); }
//		}
//	}
//	
//	public void MarkBall(lejos.robotics.geometry.Point point) {
//		if(map.containsKey(point)) {
//			map.put((Point) point, map.get(point).setBall());
//		} else {
//			map.put((Point) point, new OccupancyObject().increment().setBall());
//		}
//	}
//	
//	public boolean isOccupied(lejos.robotics.geometry.Point point) {
//		OccupancyObject occObj = map.get(point);
//		return occObj.getOccupancyValue() > OCCUPANCY_WEIGHT;
//	}
//	
//	public boolean isBall(lejos.robotics.geometry.Point point) {
//		OccupancyObject occObj = map.get(point);
//		return occObj.isBall;
//	}
//	
//	/***
//	 * Checks if there are points in a square around a point
//	 * @param location Position of the center of the square
//	 * @param size length of the sides of the square
//	 * @return whether there are points in the square
//	 */
//	public boolean checkCollisions(lejos.robotics.geometry.Point location, float size) {
//		float halfSize = size/2;
//		Point a = new Point(location.x-halfSize, location.y-halfSize);
//		Point b = new Point(location.x+halfSize, location.y+halfSize);
//		
//		for (Point p : map.keySet()) {
//			if (a.x <= p.x && p.x <= b.x && a.y <= p.y && p.y <= b.y)
//				return true;
//		}
//		
//		return false;
//	}
//	
//	private class Point extends lejos.robotics.geometry.Point implements Serializable {
//
//		private static final long serialVersionUID = -3848428052139787499L;
//
//		public Point(float f) {
//			super(f);
//			// TODO Auto-generated constructor stub
//		}
//		
//		public Point(float x, float y) {
//			super(x, y);
//			// TODO Auto-generated constructor stub
//		}
//		
//		@Override
//		public int hashCode() {
//		    long bits = java.lang.Double.doubleToLongBits(getX());
//		    bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
//		    return (((int) bits) ^ ((int) (bits >> 32)));
//		}
//	}
//	
//	private class OccupancyObject {
//		private double occupancyValue = OCCUPANCY_WEIGHT;
//		public boolean isBall = false;
//		
//		public OccupancyObject increment() {
//			occupancyValue += 0.1;
//			return this;
//		}
//		
//		public OccupancyObject setBall() {
//			isBall = true;
//			return this;
//		}
//		
//		public OccupancyObject decrement() {
//			occupancyValue -= 0.1;
//			return this;
//		}
//		
//		public double getOccupancyValue() {
//			return occupancyValue;
//		}
//	}
}
