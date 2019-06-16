package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GuiApplication extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerGUI.fxml"));
		BorderPane root = loader.load();
		stage.setTitle("Golfbot Server GUI");
		stage.setScene(new Scene(root, 1280, 720));
		stage.show();
	}

}
