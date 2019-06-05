package gui.application;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import golfbot.server.blackboard.BlackboardListener;
import golfbot.server.blackboard.BlackboardSample;

//import org.opencv.core.Mat;
//import org.opencv.imgproc.Imgproc;
//	import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lejos.robotics.geometry.Point;

public class FXController implements BlackboardListener {
	
	List<BlackboardSample> samples = new ArrayList<BlackboardSample>();
	
	
	
	@FXML
	private Button btn_test;
	@FXML
	private ComboBox<String> list_test;
	@FXML
	private Slider slider_test;
	@FXML
	private Label slider_label_test;
	@FXML
	private Button start_btn;
	@FXML
	private ImageView currentFrame;
	@FXML
	private ScatterChart<Double, Double> scanMap;
		
	
	private ScheduledExecutorService timer; 
	private boolean cameraActive = false;
	private static int cameraId = 0;
	
	@FXML
	protected void OnClickBtnTest(ActionEvent event) {
		System.out.println("XD");
		
		
		
		ObservableList<String> meme = list_test.getItems();
		
		meme.add(slider_label_test.getText());
		   
        list_test.setItems(meme);
        
        slider_test.setMin(1);
        slider_test.setMax(100);
        slider_test.setMinorTickCount(1);
        slider_test.setBlockIncrement(1);

	
        slider_test.valueProperty().addListener( 
                new ChangeListener<Number>() { 
     
               public void changed(ObservableValue <? extends Number >  
                         observable, Number oldValue, Number newValue) 
               { 
     
                   slider_label_test.setText(""+ newValue.intValue()); 
               } 
           }); 
	}

	public void startRenderThread() {
	    Task <Void> task = new Task<Void>() {
	        @Override public Void call() throws InterruptedException {
	        	int i = 0;
		        while(true) {
		          // some actions
		          Thread.sleep(500);
		          i++;
		          updateMessage("Bitch "+i);
	
		        }
	        }
	      };
	      
	      start_btn.textProperty().bind(task.messageProperty());
	      
	      Thread thread = new Thread(task);
	      thread.setDaemon(true);
	      thread.start();
	}
	

	@Override
	public void blackboardUpdated(BlackboardSample bbSample) {
		samples.add(bbSample);
		
		ObservableList<String> tmp = list_test.getItems();
		
		tmp.add("" + (tmp.size()+1));
		   
        list_test.setItems(tmp);
	}
	
	public void init() {
	    list_test.valueProperty().addListener(new ChangeListener<String>() {
	        @Override public void changed(ObservableValue ov, String val, String index) {
	        	BlackboardSample temp = samples.get(Integer.parseInt(index));
	        	
	        	
	        	scanMap.getData().clear();

	        	XYChart.Series<Double, Double> series = null;
	        	
	        	for (Entry<Double, Double> entry : temp.scan.entrySet()) {
	    		    
	    		    
	    		    double xx = entry.getKey()*(float)Math.cos(Math.toRadians(entry.getValue()));
	    		    double yy = entry.getKey()*(float)Math.sin(Math.toRadians(entry.getValue()));
	    		    
	    		    series = new Series<Double, Double>();
	    		    
	    		    series.getData().add(new XYChart.Data<Double, Double>(xx,yy));
	    		}
	        
	        	if(series != null) {
	        		scanMap.getData().addAll(series);
	        	}
	        	
	        }    
	    });
	}
}
