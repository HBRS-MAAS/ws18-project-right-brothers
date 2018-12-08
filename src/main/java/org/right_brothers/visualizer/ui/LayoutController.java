package org.right_brothers.visualizer.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

public class LayoutController implements Initializable {
	@FXML
	private AnchorPane backingStageContainer;
	
	@FXML
	private AnchorPane packagingStageContainer;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			Parent bakingStage = FXMLLoader.load(getClass().getResource("/fxml/BakingStage.fxml"));
			backingStageContainer.getChildren().add(bakingStage);
			
			Parent packaging = FXMLLoader.load(getClass().getResource("/fxml/PackagingStage.fxml"));
			packagingStageContainer.getChildren().add(packaging);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
