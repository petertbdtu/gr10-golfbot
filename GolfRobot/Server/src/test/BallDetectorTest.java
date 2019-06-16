package test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import mapping.BallDetector;
import mapping.LidarScan;
import objects.Point;

public class BallDetectorTest {
	
	public static void main(String args[])
	{
		BallDetector bd = new BallDetector();

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		for (int i = 1; i <= 98; i++) {
			try {
				FileInputStream fis = new FileInputStream("data/testScan"+i+".data");
				ObjectInputStream ois = new ObjectInputStream(fis);
				
				Mat map = bd.scanToMap((LidarScan) ois.readObject());
				Mat circles = bd.findAllBallsLidar(map);
				
				
				Point origo = bd.getImageCenterPoint();
				List<Point> ps = bd.getCircleLocsFromMat(circles);
				if (ps.size() > 0) {
					Point closest = bd.findClosestPointToPoint(ps, origo);
					System.out.println("Success "+i+"; closest="+closest.toString());
					
					Mat painted = bd.drawCirclesOnMap(map, circles);
					Imgcodecs.imwrite("data/testScan"+i+".png", painted);
				}
				else {
					System.out.println("No balls found.");
					Imgcodecs.imwrite("data/testScan"+i+"f.png", map);
				}
				ois.close();
			}
			catch (IOException e) {
				System.out.println("IOException "+i);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		/*
		FileInputStream fis = new FileInputStream("testScan4.data");
		ObjectInputStream ois = new ObjectInputStream(fis);

		LidarScan scan = (LidarScan) ois.readObject();
		ois.close();

		// graphheight in getGraph sometimes crashes if too low for data.
		//Mat graph = bd.getGraph(scan, 1);
		//Imgcodecs.imwrite("graph.png", graph);
		
		Mat map = bd.scanToMap(scan);
		Imgcodecs.imwrite("map.png", map);

		Point closestball = bd.findClosestBallLidar(scan);
		if (closestball == null) {
			System.out.println("No balls found.");
		} else {
			System.out.println("Closest ball found at "+closestball.toString());
		}

		//System.out.println(bd.scanCameraImage(CAMERA PHOTO));
		*/
	}
}
