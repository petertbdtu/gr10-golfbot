package gui.application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

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
