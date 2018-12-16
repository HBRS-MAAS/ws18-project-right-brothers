package org.right_brothers.visualizer.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PackagingCardController implements Initializable, StageController {
	@FXML
	private Label title;
	
	@FXML
	private Label description;

	@Override
	public void updateStage(String messageType, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void setText(String title, String description) {
		this.title.setText(title);
		this.description.setText(description);
	}

}
