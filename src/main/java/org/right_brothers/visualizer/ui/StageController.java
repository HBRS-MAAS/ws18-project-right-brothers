package org.right_brothers.visualizer.ui;

import java.util.ArrayList;
import java.util.List;

import org.right_brothers.visualizer.model.StageCard;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public abstract class StageController implements ScenarioAware{
	public abstract void updateStage(String messageType, String message);
	
	protected void highlightCard(Node node) {
		FadeTransition ft = new FadeTransition(Duration.millis(200), node);
		ft.setFromValue(0.5);
		ft.setToValue(1.0);
		ft.setCycleCount(1);
		ft.setAutoReverse(true);
 
		ft.play();
	}
	
	protected void cleanUp(List<? extends StageCard> cardDataList, VBox container) {
		List<StageCard> cardsToRemove = new ArrayList<>();
		List<Node> nodesToRemove = new ArrayList<>();
		
		for(int index = cardDataList.size() -1; index >=0; index--) {
			if(cardDataList.get(index).isComplete()) {
				cardsToRemove.add(cardDataList.get(index));
				nodesToRemove.add(container.getChildren().get(index));
			}
		}
		
		for(int index = cardsToRemove.size()-1; index>=0; index--) {
			cardDataList.remove(cardsToRemove.get(index));
			
			Node node = nodesToRemove.get(index);
			Platform.runLater(
					  () -> {
						  container.getChildren().remove(node);
					  }
					);
		}
	}
}
