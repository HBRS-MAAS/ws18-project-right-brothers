package org.right_brothers.visualizer.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class DeliveryStageController implements Initializable, StageController {
	@FXML
	private VBox container;

	@Override
	public void updateStage(String messageType, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/DeliveryCard.fxml"));
			Parent deliveryCard = fxmlLoader.load();
			container.getChildren().add(deliveryCard);
			
			DeliveryCardController controller =  fxmlLoader.getController();
			controller.setText("order-331", "customer-001", "Bagel(5), Bagel(2), Donut(1)");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
