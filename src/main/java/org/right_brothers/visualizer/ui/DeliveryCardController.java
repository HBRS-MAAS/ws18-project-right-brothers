package org.right_brothers.visualizer.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class DeliveryCardController implements Initializable, StageController {
	@FXML
	private Label title;
	
	@FXML
	private Label subTitle;
	
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
	
	public void setText(String title, String subTitle, String description) {
		this.title.setText(title);
		this.subTitle.setText(subTitle);
		this.description.setText(description);
		
		Tooltip toolTip = new Tooltip(description);
		this.description.setTooltip(toolTip);
	}

}
