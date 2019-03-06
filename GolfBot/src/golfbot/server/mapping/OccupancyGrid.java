package golfbot.server.mapping;

import java.util.HashMap;

import lejos.robotics.geometry.Point;

public class OccupancyGrid {
	private final double OCCUPANCY_WEIGHT = 0.5;
	private HashMap<Point,OccupancyObject> map;
	
	public OccupancyGrid() {
		this.map = new HashMap<Point,OccupancyObject>();
	}
	
	public void registerOccupancy(Point point, boolean isOccupied) {
		if(map.containsKey(point)) {
			if(isOccupied) { map.put(point, map.get(point).increment()); } 
			else { map.put(point, map.get(point).decrement()); }
		} else {
			if(isOccupied) { map.put(point, new OccupancyObject().increment()); } 
			else { map.put(point, new OccupancyObject().decrement()); }
		}
	}
	
	public void MarkBall(Point point) {
		if(map.containsKey(point)) {
			map.put(point, map.get(point).setBall());
		} else {
			map.put(point, new OccupancyObject().increment().setBall());
		}
	}
	
	public boolean isOccupied(Point point) {
		OccupancyObject occObj = map.get(point);
		return occObj.getOccupancyValue() > OCCUPANCY_WEIGHT;
	}
	
	public boolean isBall(Point point) {
		OccupancyObject occObj = map.get(point);
		return occObj.isBall;
	}
	
	class OccupancyObject {
		private double occupancyValue = OCCUPANCY_WEIGHT;
		public boolean isBall = false;
		
		public OccupancyObject increment() {
			occupancyValue += 0.1;
			return this;
		}
		
		public OccupancyObject setBall() {
			isBall = true;
			return this;
		}
		
		public OccupancyObject decrement() {
			occupancyValue -= 0.1;
			return this;
		}
		
		public double getOccupancyValue() {
			return occupancyValue;
		}
		
	}
}
