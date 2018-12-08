package org.right_brothers.visualizer.controllers;

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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			Parent bakingStage = FXMLLoader.load(getClass().getResource("../ui/BakingStage.fxml"));
			backingStageContainer.getChildren().add(bakingStage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
