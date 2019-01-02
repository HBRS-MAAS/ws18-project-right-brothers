package org.right_brothers.visualizer.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.maas.data.messages.ProductMessage;
import org.maas.utils.JsonConverter;
import org.right_brothers.data.messages.UnbakedProductMessage;

import com.fasterxml.jackson.core.type.TypeReference;

import javafx.application.Platform;
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
		// TODO Auto-generated method stub
	}

	@Override
	public void updateStage(String messageType, String message) {
		if(messageType.matches("^[\\w\\-]+\\-cooled\\-product\\-\\d+$")) {		
			ProductMessage productMessage = JsonConverter.getInstance(message, new TypeReference<ProductMessage>() {});
			addCard(productMessage);
		}
	}
	
	private void addCard(ProductMessage message) {
		List<String> products = new ArrayList<String>();
		
		for(String key: message.getProducts().keySet()) {
			products.add(String.format("%s(%s)", key, message.getProducts().get(key)));
		}
		
		Platform.runLater(
				  () -> {
					  try {
							FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/PackagingCard.fxml"));
							Parent packagingCard = fxmlLoader.load();
							container.getChildren().add(packagingCard);
							
							PackagingCardController controller =  fxmlLoader.getController();
							controller.setText("bakery-001", String.join(" ", products));
						} catch(IOException e) {
							e.printStackTrace();
						}
				  }
				);
	}
}
