package org.right_brothers.visualizer.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class PackagingStageController implements Initializable, StageController {
	@FXML
	private VBox container;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/PackagingCard.fxml"));
			Parent packagingCard = fxmlLoader.load();
			container.getChildren().add(packagingCard);
			
			PackagingCardController controller =  fxmlLoader.getController();
			controller.setText("bakery-001", "Bagel(7), Donut(1)");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateStage(String messageType, String message) {
		// TODO Auto-generated method stub
	}
}
