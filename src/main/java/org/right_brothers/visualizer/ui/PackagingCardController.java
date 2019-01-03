package org.right_brothers.visualizer.ui;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.right_brothers.visualizer.model.PackagingStageCard;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class PackagingCardController implements Initializable {
	@FXML
	private Label title;
	
	@FXML
	private Label description;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void setText(String title, String description) {
		this.title.setText(title);
		this.description.setText(description);
		
		Tooltip toolTip = new Tooltip(description);
		this.description.setTooltip(toolTip);
	}
	
	public void setText(PackagingStageCard card) {
		String title = card.getBakeryId();
		String description = card.getProducts()
				.stream()
				.map(item -> String.format("%s(%s)", item.getItemText(), item.getQuantity()))
				.collect( Collectors.joining(" "));
		
		this.title.setText(title);
		this.description.setText(description);
		
		Tooltip toolTip = new Tooltip(description);
		this.description.setTooltip(toolTip);
	}
}
