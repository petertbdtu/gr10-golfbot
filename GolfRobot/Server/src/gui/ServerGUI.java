package gui;

import org.opencv.core.Core;

import blackboard.BLCollisionDetector;
import blackboard.BlackboardController;
import blackboard.BLStateController;
import communication.CommandTransmitter;
import communication.LegoReceiver;
import communication.LidarReceiver;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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

	private LidarReceiver lidarReceiver;
	private LegoReceiver legoReceiver;
	private CommandTransmitter commandTransmitter;
	private BlackboardController bController;
    
    public ServerGUI() {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    @FXML
    private void initialize() {
    	cbStartState.getItems().setAll(BLStateController.State.values());
    }
     
    @FXML
    private void onClickStart() 
    {
    	collisionDetector = new BLCollisionDetector();
    	bController.registerListener(collisionDetector);
    	
    	stateController = new BLStateController(commandTransmitter, collisionDetector, cbStartState.getValue());
    	bController.registerListener(stateController);
    	
    	stateController.start();
    	collisionDetector.start();
    }
    
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
        if(stateController != null) {
        	stateController.stopStateMachine = true;
        	bController.removeListener(stateController);
        }
        
        if(collisionDetector != null) {
        	collisionDetector.keepDetecting = false;
        	bController.removeListener(collisionDetector);
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
		lidarReceiver = new LidarReceiver();
		lidarReceiver.bindSocket(5000);
		lidarReceiver.start();
    	circleLidar.setFill(Color.RED);

		legoReceiver = new LegoReceiver();
		legoReceiver.connect(3000);
		legoReceiver.start();
    	circleLego.setFill(Color.RED);

		commandTransmitter = new CommandTransmitter();
		commandTransmitter.connect(3001);
    	circleTransmitter.setFill(Color.RED);

		bController = new BlackboardController(null, legoReceiver, lidarReceiver);
    }
    
    @FXML
    private void onClickStopNetwork() 
    {
    	if(lidarReceiver != null)
    		lidarReceiver.stopReceiver();
    	circleLidar.setFill(Color.RED);

    	
    	if(legoReceiver != null)
    		legoReceiver.stopReceiver();
    	circleLego.setFill(Color.RED);

    	
    	if(lidarReceiver != null)
    		commandTransmitter.stopTransmitter();
    	circleTransmitter.setFill(Color.RED);

    	if(bController != null)
    		bController.stopBlackboard();
    }
    
    public void setState(String newState) {
		this.lblStateValue.setText(newState);
	}

	public void setIsMoving(String isMoving) {
		this.lblMovingValue.setText(isMoving);
	}

	public void setIsCollecting(String isCollecting) {
		this.lblCollectingValue.setText(isCollecting);
	}

	public void setCollisionDetected(String collisionDetected) {
		this.lblCollisionValue.setText(collisionDetected);
	}

	public void setLastMove(String lastMove) {
		this.lblMoveValue.setText(lastMove);
	}

	public void setBallLocation(String ballLocation) {
		this.lblBallLocationValue.setText(ballLocation);
	}

	public void setBallHeading(String ballHeading) {
		this.lblBallHeadingValue.setText(ballHeading);
	}

	public void setBallDistance(String ballDistance) {
		this.lblBallDistanceValue.setText(ballDistance);
	}

	public void setBallsCollected(String ballsCollected) {
		this.lblCollectedValue.setText(ballsCollected);
	}

	public void setTimer(String timerValue) {
		this.lblTimerValue.setText(timerValue);
	}
}
