package gui;

import java.io.ByteArrayInputStream;

import org.opencv.core.Core;

import blackboard.BlackboardController;
import blackboard.BLStateController;
import communication.CommandTransmitter;
import communication.LegoReceiver;
import communication.LidarReceiver;
import deprecated.CameraReceiver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ServerGUI {

    @FXML
    private Label 	lblStateValue, lblMovingValue, lblGoalValue, lblMoveValue, 
    				lblBallLocationValue, lblBallHeadingValue, lblBallDistanceValue, lblCollectedValue, lblTimerValue;
    
	@FXML
    private ChoiceBox<BLStateController.State> cbStartState;
	
    @FXML
    private Button 	btnStart, btnPause, btnStop, btnIncrement, btnDecrement,
    				btnStartNetwork, btnStopNetwork;
    
    @FXML
    private Circle circleLidar, circleLego, circleTransmitter;
    
    @FXML
    private ImageView ivLidar, ivLidarAnalyzed;
    private BLStateController stateController;

    private Thread networkThread;
	private CameraReceiver cameraReceiver;
	private LidarReceiver lidarReceiver;
	private LegoReceiver legoReceiver;
	private CommandTransmitter commandTransmitter;
	private BlackboardController bbController;
	private ServerGUI serverGUI;
	private volatile byte[] imgLidar = new byte[1];
	private volatile byte[] imgLidarAnalyzed = new byte[1];
	private long stopTime = 0;
	private long startTime = 0;
	private String collected = "";
	private String distanceBall = "";
	private String headingBall = "";
	private String locationBall = "";
	private String moveLast = "";
	private String goalFinding = "";
	private String moving = "";
	private String state = "";
	
	private boolean timer_isRunning = false;
	Thread updater;

    
    public ServerGUI() {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    @FXML
    private void initialize() {
    	serverGUI = this;
    	cbStartState.getItems().setAll(BLStateController.State.values());
    }
    
    @FXML
    private void onClickStart() 
    {
    	stateController = new BLStateController(this, commandTransmitter, cbStartState.getValue());
    	bbController.registerListener(stateController);
    	stateController.start();
    }
    
    @FXML
    private void onClickPause() 
    {
        if(stateController != null) {
        	if(stateController.pauseStateMachine) {
            	stateController.pauseStateMachine = false;
            	btnPause.setText("Pause Robot");
            } else {
            	stateController.pauseStateMachine = true;
            	btnPause.setText("Resume Robot");
            }
        }
    }
    
    @FXML
    private void onClickStop() 
    { 
        if(stateController != null) {
        	stateController.stopStateMachine = true;
        	stateController = null;
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
//        	circleCamera.setFill(Color.YELLOW);
        	circleLego.setFill(Color.YELLOW);
        	circleTransmitter.setFill(Color.YELLOW);
        	
        	updater = new Thread(() -> {
	    		while(true) {
	    			try {
	    				
	    				ivLidar.setImage(new Image(new ByteArrayInputStream(imgLidar)));
	    				ivLidarAnalyzed.setImage(new Image(new ByteArrayInputStream(imgLidarAnalyzed)));
	    				Platform.runLater(() -> lblBallDistanceValue.setText(distanceBall));
	    				Platform.runLater(() -> lblBallHeadingValue.setText(headingBall));
	    				Platform.runLater(() -> lblBallLocationValue.setText(locationBall));
	    				Platform.runLater(() -> lblCollectedValue.setText(collected));
	    				Platform.runLater(() -> lblGoalValue.setText(goalFinding));
	    				Platform.runLater(() -> lblMoveValue.setText(moveLast));
	    				Platform.runLater(() -> lblMovingValue.setText(moving));
	    				Platform.runLater(() -> lblStateValue.setText(state));
	    				if(timer_isRunning) {
		    				Platform.runLater(() -> lblTimerValue.setText(String.format("%02d:%02d", ((System.currentTimeMillis() - startTime) / 1000) / 60, ((System.currentTimeMillis() - startTime) / 1000) % 60)));
	    				} else {
		    				Platform.runLater(() -> lblTimerValue.setText(String.format("%02d:%02d", ((stopTime - startTime) / 1000) / 60, ((stopTime - startTime) / 1000) % 60)));
	    				}
	 
	    				Thread.sleep(250); 
    				} catch (InterruptedException e) { }
	    		}
        	});
        	updater.start();
        	
        	networkThread = new Thread(() -> {
    			bbController = new BlackboardController(serverGUI);
    			bbController.start();
    			
//    			cameraReceiver = new CameraReceiver();
//    			if(cameraReceiver.connect(6000)) {
//    				bbController.addCameraReceiver(cameraReceiver);
//    				cameraReceiver.start();
//    				circleCamera.setFill(Color.LIGHTGREEN);
//    			} else {
//    				cameraReceiver = null;
//    				circleCamera.setFill(Color.RED);
//    			}
    			
    			lidarReceiver = new LidarReceiver();
    			if(lidarReceiver.bindSocket(5000)) {
    				bbController.addLidarReceiver(lidarReceiver);
        			lidarReceiver.start();
        	    	circleLidar.setFill(Color.LIGHTGREEN);
    			} else {
    				lidarReceiver = null;
        	    	circleLidar.setFill(Color.RED);
    			}
    			legoReceiver = new LegoReceiver();
    			if(legoReceiver.connect(3000)) {
    				bbController.addLegoReceiver(legoReceiver);
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
    	
    	if(lidarReceiver != null) {
    		lidarReceiver.stopReceiver();
    		lidarReceiver = null;
    	}
    	circleLidar.setFill(Color.RED);

    	if(cameraReceiver != null) {
    		cameraReceiver.stopReceiver();
    		cameraReceiver = null;
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
		
    	if(bbController != null) {
    		bbController.stopBlackboard();
    		bbController = null;
    	}
    }
    
    public void setState(String newState) {
    	state = new String(newState);
	}

	public void setIsMoving(String isMoving) {
		moving = new String(isMoving);
	}

	public void setGoalFinding(String isGoalFinding) {
		goalFinding = new String(isGoalFinding);
	}

	public void setLastMove(String lastMove) {
		moveLast = new String(lastMove);
	}

	public void setBallLocation(String ballLocation) {
		locationBall = new String(ballLocation);
	}

	public void setBallHeading(String ballHeading) {
		headingBall = new String(ballHeading);
	}

	public void setBallDistance(String ballDistance) {
		distanceBall = new String(ballDistance);
	}

	public void setBallsCollected(String ballsCollected) {
		collected = new String(ballsCollected);
	}
	
	public void setStartTime(long timerValue) {
		startTime = timerValue;
		timer_isRunning = true;
	}
	
	public void setStopTime(long timeValue) {
		stopTime = timeValue;
		timer_isRunning = false;
	}
	
	public void setLidarScan(byte[] img) {
		imgLidar = img.clone();
	}
	
	public void setLidarAnalyzedFrame(byte[] imageBuffer) {
		imgLidarAnalyzed = imageBuffer.clone();
	}
}
