package test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import blackboard.BLBallDetector;
import mapping.LidarScan;
import objects.Point;

public class BallDetectorTest {
	
	public static void main(String args[])
	{
		BLBallDetector bd = new BLBallDetector();

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		for (int i = 1; i <= 1835; i++) {
			try {
				FileInputStream fis = new FileInputStream("data/testScan"+i+".data");
				ObjectInputStream ois = new ObjectInputStream(fis);
				Point p = bd.findClosestBallLidar((LidarScan) ois.readObject());
				System.out.println("success "+i+"; point="+p.toString());
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
