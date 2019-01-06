package org.right_brothers.visualizer.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import org.maas.utils.Time;
import org.right_brothers.visualizer.model.TimelineItem;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LayoutController implements Initializable, ScenarioAware {
	private Stage container;
	private Time currentTime;
	
	private boolean isReplaying = false;
	
	private List<TimelineItem> timelineItems;
	
	@FXML
	private AnchorPane backingStageContainer;
	
	@FXML
	private AnchorPane packagingStageContainer;
	
	@FXML
	private AnchorPane deliveryStageContainer;
	
	@FXML
	private Label timeDisplay;
	
	private List<StageController> controllers = new ArrayList<>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		timelineItems = new ArrayList<>();
		
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
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void updateBoard(String messageType, String message) {
		timelineItems.add(new TimelineItem(currentTime, messageType, message));
		
		if(!isReplaying) {
			for(StageController controller: controllers) {
				controller.updateStage(messageType, message);
			}
		}
	}

	@Override
	public void setScenario(String scenarioDirectory) {
		for(StageController controller: controllers) {
			controller.setScenario(scenarioDirectory);
		}
	}

	public void setTime(Time currentTime) {
		this.currentTime = currentTime;
		
		if(!isReplaying) {
			Platform.runLater(
					  () -> {
						  timeDisplay.setText(currentTime.toString());
					  }
					);
		}
	}
	
	@FXML
    private void handleExitAction(ActionEvent event) {
        System.out.println("exiting");
        container.close();
    }
	
	@FXML
    private void handleReplayAction(ActionEvent event) throws InterruptedException {
        isReplaying = true;
        
//    	for(StageController controller: controllers) {
//			controller.clear();
//		}
//        
//        for(TimelineItem item: timelineItems) {
//        	for(StageController controller: controllers) {
//				controller.updateStage(item.getMessageType(), item.getMessage());
//			}
//
//        	Platform.runLater(
//					  () -> {
//						  timeDisplay.setText(item.getTime().toString());
//					  }
//					);
//        }
    }

	public void setStage(Stage primaryStage) {
		container = primaryStage;
	}
}
