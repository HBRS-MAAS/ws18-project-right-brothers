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
import org.right_brothers.data.messages.UnbakedProductMessage;
import org.right_brothers.visualizer.model.BakingStageCard;
import org.right_brothers.visualizer.model.CardItem;

import com.fasterxml.jackson.core.type.TypeReference;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class BakingStageController extends StageController implements Initializable {
	@FXML
	private VBox container;
	
	@FXML
	private Label cardCount;
	
	private List<BakingStageCard> cardDataList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cardDataList = new ArrayList<>();
	}

	@Override
	public void updateStage(String messageType, String message) {
		Matcher bakingRequestMatcher = Pattern.compile("^([\\w\\-]+)\\-baking-request$")
				.matcher(messageType);
		Matcher cooledProductConversationMatcher = Pattern.compile("^([\\w\\-]+)\\-cooled\\-product\\-(\\d+)$")
				.matcher(messageType);
		
		if(bakingRequestMatcher.matches()) {
			UnbakedProductMessage unbakedProductMessage = JsonConverter
					.getInstance(message, new TypeReference<UnbakedProductMessage>() {});
			String bakeryId = bakingRequestMatcher.group(1);
			
			addCard(bakeryId, unbakedProductMessage);
		} else if(cooledProductConversationMatcher.matches()) {		
			String bakeryId = cooledProductConversationMatcher.group(1);
			
			ProductMessage productMessage = JsonConverter.getInstance(message, new TypeReference<ProductMessage>() {});
			removeCard(bakeryId, productMessage);
		}
		
		Platform.runLater(
				  () -> {
					  cardCount.setText(Integer.toString(container.getChildren().size()));
				  }
				);
		
	}

	private void addCard(String bakeryId, UnbakedProductMessage message) {
		List<String> orders = new ArrayList<String>();
		List<CardItem> cardItems = new ArrayList<>();
		
		for(int i=0; i< message.getGuids().size(); i++) {
			orders.add(String.format("%s(%s)", 
					message.getGuids().get(i), message.getProductQuantities().get(i)));
			
			cardItems.add(new CardItem(
						message.getGuids().get(i), 
						message.getProductQuantities().get(i)
					));
		}
		
		Platform.runLater(
				  () -> {
					  try {
							FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/right_brothers/BakingCard.fxml"));
							Parent bakingCardNode = fxmlLoader.load();
							
							BakingCardController controller =  fxmlLoader.getController();
							bakingCardNode.setUserData(controller);
							
							container.getChildren().add(0, bakingCardNode);

							BakingStageCard bakingStageCard = new BakingStageCard(
										bakeryId, message.getProductType(), cardItems
									);
							controller.setText(bakingStageCard);
							cardDataList.add(0, bakingStageCard);
							
							highlightCard(bakingCardNode);
						} catch(IOException e) {
							e.printStackTrace();
						}
				  }
				);
	}
	
	private void removeCard(String bakeryId, ProductMessage productMessage) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setScenario(String scenarioDirectory) {
		// TODO Auto-generated method stub
		
	}

}
