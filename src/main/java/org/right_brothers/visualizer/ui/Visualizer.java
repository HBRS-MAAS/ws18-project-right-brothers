package org.right_brothers.visualizer.ui;
	
import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Visualizer extends Application {
	private static final CountDownLatch countDownLatch = new CountDownLatch(1);
	public static Visualizer currentInstance = null;
	
	private LayoutController layoutController;
	
	public Visualizer() {
		// setInstance(this);
	}
	
	public static void setInstance(Visualizer visulizer) {
        currentInstance = visulizer;
        countDownLatch.countDown();
    }
	
	// https://stackoverflow.com/questions/25873769/launch-javafx-application-from-another-class
    public static Visualizer waitForInstance() {
        try {
        	countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return currentInstance;
    }
	
	public static void run(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Layout.fxml"));
			Parent root = fxmlLoader.load();
			layoutController = fxmlLoader.getController();
			
			
			Scene scene = new Scene(root);
			scene.getStylesheets()
				.add(getClass().getResource("/fxml/application.css").toExternalForm());
			
			primaryStage.setScene(scene);
			primaryStage.setMaximized(true);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		setInstance(this);
	}
	
	@Override
	public void stop() {
		System.out.println("Closing application");
	}
	
	public void updateBoard(String messageType, String message) {
		layoutController.updateBoard(messageType, message);
	}
}
