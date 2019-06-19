package deprecated;
//package communication;
//
//
//
//public class SlidingPointsLocalization {
//	
//	private static final String PATH = "src\\golfbot\\server\\mapping\\TestData";
//
//	public static void main(String[] args) {
//		BLOccupancyGrid map = BLOccupancyGrid.loadTestData(PATH);
//		Point location = getLocation(map, generateScan(createTestDataSample()));
//		System.out.println(location.x + "," + location.y);
//	}
//	
//	public static Point getLocation(BLOccupancyGrid map, BLOccupancyGrid scan) {
//		Point position = new Point(0,0);
//		//TranslateX = 
//		
//		// Set the location of the window
//		Point windowLocation = new Point(18, 14);
//		int validationPointsMax = -1, validationX = -1, validationY = -1;
//		
//		for (int x = (int)map.minX; x < (int)map.maxX; x++) {
//		    for (int y = (int)map.minX; y < (int)map.maxY; y++) {
//		    	int curScanPoints = validateScanPoints();
//		    	if(validateScanPoints() > validationPointsMax) {
//		    		validationPointsMax = curScanPoints;
//		    		validationX = x;
//		    		validationY = y;
//		    	}
//		    }
//		}
//		return null;
//	}
//	
//	private static int validateScanPoints() {
//		return 0;
//        //DoSomethingWith(array[(windowLocation.X + x) % arraySize.Width,
//          //                    (windowLocation.Y + y) % arraySize.Height]);
//	}
//	
//	private static BLOccupancyGrid generateScan(LaserSample[] samples) {
//		BLOccupancyGrid scan = new BLOccupancyGrid();
//		Point point = new Point(0,0);
//		for(LaserSample sample : samples) {
//			scan.registerOccupancy(point.pointAt(sample.distance, sample.angle), true);
//		}
//		
//		return scan;	
//	}
//	
//	private static BLOccupancyGrid createTestDataMap(BLOccupancyGrid og) {
//		if(og == null)
//			og = new BLOccupancyGrid();
//		
//		for(int i=0 ; i<=1000 ; i++ ) {
//			float x1 = 0, y1 = 0, x2 = 0, y2= 0;
//			if(i < 500) {
//				x1 += i; y1 = 0;
//				x2 = 0; y2 += i;
//			} else {
//				x1 = 500; y1 = i-500;
//				x2 = i-500; y2 = 500;
//			}
//			og.registerOccupancy(new Point(x1,y1), true);
//			og.registerOccupancy(new Point(x2,y2), true);
//		}
//		
//		return og;
//	}
//	
//	public static LaserSample[] createTestDataSample() {
//		return new LaserSample[] {
//				new LaserSample(0,2),
//				new LaserSample(90, 497),
//				new LaserSample(180, 497), 
//				new LaserSample(270, 2)
//		};
//	}
//}
//
//class LaserSample {
//	public float angle;
//	public float distance;
//	
//	public LaserSample(float angle, float distance) {
//		this.angle = angle;
//		this.distance = distance;
//	}
//}
