package gui;

import java.io.ByteArrayInputStream;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import blackboard.BLCollisionDetector;
import blackboard.BlackboardController;
import blackboard.BLStateController;
import communication.CommandTransmitter;
import communication.LegoReceiver;
import communication.LidarReceiver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import mapping.LidarAnalyser;
import mapping.Vision;

public class ServerGUI {

    @FXML
    private Label 	lblStateValue, lblMovingValue, lblCollectingValue, lblCollisionValue, lblMoveValue, 
    				lblBallLocationValue, lblBallHeadingValue, lblBallDistanceValue, lblCollectedValue, lblTimerValue;
    
	@FXML
    private ChoiceBox<BLStateController.State> cbStartState;
	
    @FXML
    private Button 	btnStart, btnPause, btnStop, btnIncrement, btnDecrement,
    				btnStartNetwork, btnStopNetwork;
    
    @FXML
    private Circle circleLidar, circleLego, circleTransmitter;
    
    @FXML
    private ImageView ivLidar, ivCamera;
    
    private BLStateController stateController;
    private BLCollisionDetector collisionDetector;

    private Thread networkThread;
	private LidarReceiver lidarReceiver;
	private LidarAnalyser lidarAnalyser;
	private LegoReceiver legoReceiver;
	private CommandTransmitter commandTransmitter;
	private BlackboardController bController;
    
    public ServerGUI() {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    @FXML
    private void initialize() {
    	cbStartState.getItems().setAll(BLStateController.State.values());
    	Mat mat = Vision.readImageFromFile("hammer.jpg");
    	setLidarScan(Vision.matToImageBuffer(mat));
    }
    
  
	@FXML
	private void onClickStart() {
    	Mat mat = Vision.readImageFromFile("TestImage.png");
    	Mat lines = Vision.findLines(mat);
    	setLidarScan(Vision.matToImageBuffer(lines));
	}
     
//    @FXML
//    private void onClickStart() 
//    {
//		bController = new BlackboardController(null, legoReceiver, lidarReceiver);
//    	
//    	collisionDetector = new BLCollisionDetector();
//    	bController.registerListener(collisionDetector);
//    	
//    	stateController = new BLStateController(this, commandTransmitter, collisionDetector, cbStartState.getValue());
//    	bController.registerListener(stateController);
//    	
//		bController.start();
//    	stateController.start();
//    	collisionDetector.start();
//    }
    
    @FXML
    private void onClickPause() 
    {
        if(stateController != null && stateController.pauseStateMachine) {
        	stateController.pauseStateMachine = false;
        	btnPause.setText("Pause Robot");
        } else {
        	stateController.pauseStateMachine = true;
        	btnPause.setText("Resume Robot");
        }
    }
    
    @FXML
    private void onClickStop() 
    { 
    	if(bController != null) {
    		bController.stopBlackboard();
    		bController = null;
    	}
    	
        if(stateController != null) {
        	stateController.stopStateMachine = true;
        	stateController = null;
        }
        
        if(collisionDetector != null) {
        	collisionDetector.keepDetecting = false;
        	collisionDetector = null;
        }
    }
    
    @FXML
    private void onClickIncrement() 
    {
    	if(stateController != null) {
    		stateController.ballCollectedCount++;
    		setBallsCollected(stateController.ballCollectedCount + "");
    	}
    }
    
    @FXML
    private void onClickDecrement() 
    {
    	if(stateController != null) {
    		stateController.ballCollectedCount--;
    		setBallsCollected(stateController.ballCollectedCount + "");
    	}
    }
    
    @FXML
    private void onClickStartNetwork() 
    {
    	if(networkThread == null || !networkThread.isAlive()) {
        	circleLidar.setFill(Color.YELLOW);
        	circleLego.setFill(Color.YELLOW);
        	circleTransmitter.setFill(Color.YELLOW);
        	ServerGUI tmpGUI = this;
        	networkThread = new Thread(new Runnable() {
        		@Override
        		public void run() {
        			lidarReceiver = new LidarReceiver();
        			if(lidarReceiver.bindSocket(5000)) {
	        			lidarReceiver.start();
	        			lidarAnalyser = new LidarAnalyser(lidarReceiver, tmpGUI);
	        			lidarAnalyser.start();
	        	    	circleLidar.setFill(Color.LIGHTGREEN);
        			} else {
        				lidarReceiver = null;
	        	    	circleLidar.setFill(Color.RED);
        			}
        	    	
        			legoReceiver = new LegoReceiver();
        			if(legoReceiver.connect(3000)) {
        				legoReceiver.start();
            	    	circleLego.setFill(Color.LIGHTGREEN);
        			} else {
        				legoReceiver = null;
        				circleLego.setFill(Color.RED);
        			}

        			commandTransmitter = new CommandTransmitter();
        			if(commandTransmitter.connect(3001)) {
        				circleTransmitter.setFill(Color.LIGHTGREEN);
        			} else {
        				commandTransmitter = null;
        				circleTransmitter.setFill(Color.RED);
        			}
        		}
        	});
        	networkThread.start();
    	}
    }
    
    @SuppressWarnings("deprecation")
	@FXML
    private void onClickStopNetwork() 
    {
    	if(networkThread != null && networkThread.isAlive()) {
    		networkThread.stop();
        	networkThread = null;
    	}
    		
    	if(lidarAnalyser != null) {
    		lidarAnalyser.keepAlive = false;
    		lidarAnalyser = null;
    	}
    	
    	if(lidarReceiver != null) {
    		lidarReceiver.stopReceiver();
    		lidarReceiver = null;
    	}
    	circleLidar.setFill(Color.RED);

    	
    	if(legoReceiver != null) {
    		legoReceiver.stopReceiver();
    		legoReceiver = null;
    	}
    	circleLego.setFill(Color.RED);

    	
    	if(commandTransmitter != null) {
    		commandTransmitter.closeConnections();
    		commandTransmitter = null;
    	}
		circleTransmitter.setFill(Color.RED);
    }
    
    public void setState(String newState) {
    	Platform.runLater(() -> {
    		this.lblStateValue.setText(newState);
    	});
	}

	public void setIsMoving(String isMoving) {
		Platform.runLater(() -> {
			this.lblMovingValue.setText(isMoving);
		});
	}

	public void setIsCollecting(String isCollecting) {
		Platform.runLater(() -> {
			this.lblCollectingValue.setText(isCollecting);
		});
	}

	public void setCollisionDetected(String collisionDetected) {
		Platform.runLater(() -> {
			this.lblCollisionValue.setText(collisionDetected);
		});
	}

	public void setLastMove(String lastMove) {
		Platform.runLater(() -> {
			this.lblMoveValue.setText(lastMove);
		});
	}

	public void setBallLocation(String ballLocation) {
		Platform.runLater(() -> {
			this.lblBallLocationValue.setText(ballLocation);
		});
	}

	public void setBallHeading(String ballHeading) {
		Platform.runLater(() -> {
			this.lblBallHeadingValue.setText(ballHeading);
		});
	}

	public void setBallDistance(String ballDistance) {
		Platform.runLater(() -> {
			this.lblBallDistanceValue.setText(ballDistance);
		});
	}

	public void setBallsCollected(String ballsCollected) {
		Platform.runLater(() -> {
			this.lblCollectedValue.setText(ballsCollected);
		});
	}

	public void setTimer(String timerValue) {
		Platform.runLater(() -> {
			this.lblTimerValue.setText(timerValue);
		});
	}
	
	public void setLidarScan(byte[] imageBuffer) {
		Platform.runLater(() -> {
			ivLidar.setImage(new Image(new ByteArrayInputStream(imageBuffer))); 
		});
	}
}
