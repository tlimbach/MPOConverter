package main;

import javafx.application.Application;
import javafx.stage.Stage;

public class MPOConverter extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		new MainUi(stage);
	}

	@Override
	public void init() throws Exception {
		super.init();
	}

	public static void main(String[] args) {
		launch(args);    
	}

}
