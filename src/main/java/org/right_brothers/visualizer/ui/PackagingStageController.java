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
import org.right_brothers.data.messages.LoadingBayBox;
import org.right_brothers.data.messages.LoadingBayMessage;
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
		
		Matcher packagedOrderMatcher = Pattern.compile("^([\\w\\-]+)\\-packaged-orders$")
				.matcher(messageType);
		
		if(cooledProductConversationMatcher.matches()) {
			String bakeryId = cooledProductConversationMatcher.group(1);
			
			ProductMessage productMessage = JsonConverter.getInstance(message, new TypeReference<ProductMessage>() {});
			addCard(bakeryId, productMessage);
		} else if(packagedOrderMatcher.matches()) {
			String bakeryId = packagedOrderMatcher.group(1);
			
			LoadingBayMessage loadingBayMessage = JsonConverter.getInstance(message, new TypeReference<LoadingBayMessage>() {});
			removeCards(bakeryId, loadingBayMessage);
		}
		
		Platform.runLater(
				  () -> {
					  cardCount.setText(Integer.toString(container.getChildren().size()));
				  }
				);
	}

	private void addCard(String bakeryId, ProductMessage message) {
		List<CardItem> cardItems = new ArrayList<>();
		
		for(String key: message.getProducts().keySet()) {
			cardItems.add(new CardItem(key, message.getProducts().get(key)));
		}
		
		Platform.runLater(
				  () -> {
					  try {
							FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/PackagingCard.fxml"));
							Parent packagingCardParent = fxmlLoader.load();
							
							PackagingCardController controller =  fxmlLoader.getController();
							
							packagingCardParent.setUserData(controller);
							container.getChildren().add(0, packagingCardParent);
							
							PackagingStageCard packagingStageCard = new PackagingStageCard(bakeryId, cardItems);
							cards.add(0, packagingStageCard);
							controller.setText(packagingStageCard);
							
						} catch(IOException e) {
							e.printStackTrace();
						}
				  }
				);
	}
	
	private void removeCards(String bakeryId, LoadingBayMessage loadingBayMessage) {
		// Find the packaging cards that should be updated based on delivery message
		for(int index = cards.size() -1; index >=0; index--) { 
			if(cards.get(index).getBakeryId().equalsIgnoreCase(bakeryId)) {
				boolean altered = false;
				for(LoadingBayBox box:loadingBayMessage.getBoxes()) {
					int quantityInBox = box.getQuantity();
					
					for(CardItem item:cards.get(index).getProducts()) {
						if(quantityInBox >0 && item.getItemText().equalsIgnoreCase(box.getProductType())) {
							if(quantityInBox >= item.getQuantity()) {
								quantityInBox = quantityInBox - item.getQuantity();
								item.setQuantity(0);
								altered = true;
							} else {
								item.setQuantity(item.getQuantity() - quantityInBox);
								quantityInBox = 0;
								altered = true;
							}
						}
					}
				}
				// Re render the card if altered
				if(altered) {
					PackagingStageCard alteredCard = cards.get(index);
					PackagingCardController controller = (PackagingCardController) container.getChildren().get(index).getUserData();
					
					Platform.runLater(
							  () -> {
								  controller.setText(alteredCard);
							  }
							);
				}
			}
		}
	}

	@Override
	public void setScenario(String scenarioDirectory) {
		// TODO Auto-generated method stub
		
	}
}
