package org.right_brothers.visualizer.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.maas.utils.JsonConverter;
import org.right_brothers.data.messages.UnbakedProductMessage;

import com.fasterxml.jackson.core.type.TypeReference;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class BakingStageController implements Initializable, StageController {
	@FXML
	private VBox container;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateStage(String messageType, String message) {
		if(messageType.matches("^baking-request$")) {
			UnbakedProductMessage unbakedProductMessage = JsonConverter
					.getInstance(message, new TypeReference<UnbakedProductMessage>() {});
			
			addCard(unbakedProductMessage);
		} else if(messageType.matches("^[\\w\\-]+\\-cooled\\-product\\-\\d+$")) {		
			// TODO - Remove corresponding card from baking stage
		}
	}
	
	private void addCard(UnbakedProductMessage message) {
		List<String> orders = new ArrayList<String>();
		
		for(int i=0; i< message.getGuids().size(); i++) {
			orders.add(String.format("%s(%s)", 
					message.getGuids().get(i), message.getProductQuantities().get(i)));
		}
		
		Platform.runLater(
				  () -> {
					  try {
							FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/BakingCard.fxml"));
							Parent bakingCard = fxmlLoader.load();
							container.getChildren().add(0, bakingCard);
							
							BakingCardController controller =  fxmlLoader.getController();

							controller.setText(message.getProductType(), String.join(" ", orders));
						} catch(IOException e) {
							e.printStackTrace();
						}
				  }
				);
	}

}
