package gui.application;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
	import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FXController {
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
		
	
	private ScheduledExecutorService timer; 
	private VideoCapture capture = new VideoCapture();
	private boolean cameraActive = false;
	private static int cameraId = 0;
	
	@FXML
	protected void startCamera(ActionEvent event) {
		if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open(cameraId);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run()
					{
						// effectively grab and process a single frame
						Mat frame = grabFrame();
						// convert and show the frame
						Image imageToShow = SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
						updateImageView(currentFrame, imageToShow);
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.start_btn.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.start_btn.setText("Start Camera");
			
			// stop the timer
			this.stopAcquisition();
		}
}
	
	//STOLEN CONTENT https://github.com/opencv-java/getting-started/blob/master/FXHelloCV/src/it/polito/elite/teaching/cv/utils/Utils.java
	private static BufferedImage matToBufferedImage(Mat original)
	{
		// init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);
		
		if (original.channels() > 1)
		{
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		}
		else
		{
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
		
		return image;
}
// stolen end
	private Mat grabFrame()
	{
		// init everything
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened())
		{
			try
			{
				// read the current frame
				this.capture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty())
				{
					Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
				}
				
			}
			catch (Exception e)
			{
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		
		return frame;
		
		
	}
	
	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition()
	{
		if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
		}
	}
	

	private void updateImageView(ImageView view, Image image)
	{
		onFXThread(view.imageProperty(), image);
	}
	
	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed()
	{
		this.stopAcquisition();
}
	
	public static <T> void onFXThread(final ObjectProperty<T> property, final T value)
	{
		Platform.runLater(() -> {
			property.set(value);
		});
	}

	
	
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


}
