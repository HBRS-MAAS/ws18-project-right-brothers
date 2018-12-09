package org.right_brothers.visualizer.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

public class BakingStageController implements Initializable, StageController {
	@FXML
	private VBox dummyCard;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateStage(String messageType, String message) {
		if(message!=null && message.equalsIgnoreCase("2")) {
			dummyCard.setVisible(false);
		}
	}

}
