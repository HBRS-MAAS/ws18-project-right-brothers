package org.right_brothers.visualizer.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.maas.data.messages.ProductMessage;
import org.maas.utils.JsonConverter;
import org.right_brothers.bakery_objects.CooledProduct;
import org.right_brothers.data.messages.UnbakedProductMessage;
import org.right_brothers.visualizer.model.CardItem;
import org.right_brothers.visualizer.model.PackagingStageCard;

import com.fasterxml.jackson.core.type.TypeReference;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PackagingStageController implements Initializable, ScenarioAware, StageController {
	@FXML
	private VBox container;
	
	@FXML
	private Label cardCount;
	
	private List<PackagingStageCard> cards;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cards = new ArrayList<PackagingStageCard>();
	}

	@Override
	public void updateStage(String messageType, String message) {
		Matcher cooledProductConversationMatcher = Pattern.compile("^([\\w\\-]+)\\-cooled\\-product\\-(\\d+)$")
				.matcher(messageType);
		
		if(cooledProductConversationMatcher.matches()) {
			String bakeryId = cooledProductConversationMatcher.group(1);
			
			ProductMessage productMessage = JsonConverter.getInstance(message, new TypeReference<ProductMessage>() {});
			addCard(bakeryId, productMessage);
		}
		
		Platform.runLater(
				  () -> {
					  cardCount.setText(Integer.toString(container.getChildren().size()));
				  }
				);
	}
	
	private void addCard(String bakeryId, ProductMessage message) {
		List<String> products = new ArrayList<String>();
		List<CardItem> cardItems = new ArrayList<>();
		
		for(String key: message.getProducts().keySet()) {
			products.add(String.format("%s(%s)", key, message.getProducts().get(key)));
			cardItems.add(new CardItem(key, message.getProducts().get(key)));
		}
		
		Platform.runLater(
				  () -> {
					  try {
							FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/PackagingCard.fxml"));
							Parent packagingCard = fxmlLoader.load();
							container.getChildren().add(0, packagingCard);
							
							PackagingCardController controller =  fxmlLoader.getController();
							controller.setText(bakeryId, String.join(" ", products));
							
							cards.add(0, new PackagingStageCard(bakeryId, cardItems));
						} catch(IOException e) {
							e.printStackTrace();
						}
				  }
				);
	}

	@Override
	public void setScenario(String scenarioDirectory) {
		// TODO Auto-generated method stub
		
	}
}
