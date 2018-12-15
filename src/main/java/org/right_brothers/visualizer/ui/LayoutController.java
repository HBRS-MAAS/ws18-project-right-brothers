package org.right_brothers.visualizer.ui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class LayoutController implements Initializable {
	@FXML
	private AnchorPane backingStageContainer;
	
	@FXML
	private AnchorPane packagingStageContainer;
	
	@FXML
	private AnchorPane deliveryStageContainer;
	
	private List<StageController> controllers = new Vector<>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/BakingStage.fxml"));
			Parent bakingStage = fxmlLoader.load();
			backingStageContainer.getChildren().add(bakingStage);
			controllers.add(fxmlLoader.getController());
			
			fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/PackagingStage.fxml"));
			Parent packaging = fxmlLoader.load();
			packagingStageContainer.getChildren().add(packaging);
			controllers.add(fxmlLoader.getController());
			
			fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/DeliveryStage.fxml"));
			Parent delivery = fxmlLoader.load();
			deliveryStageContainer.getChildren().add(delivery);
			controllers.add(fxmlLoader.getController());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void updateBoard(String messageType, String message) {
		for(StageController controller: controllers) {
			controller.updateStage(messageType, message);
		}
	}
}
