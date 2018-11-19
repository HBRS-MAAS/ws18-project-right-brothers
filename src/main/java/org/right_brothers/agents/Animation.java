package org.right_brothers.agents;


import javafx.animation.TranslateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.FadeTransition;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.CountDownLatch;

public class Animation extends Application {

    public Text txt;
    public Thread myThread;
    public static final CountDownLatch latch = new CountDownLatch(1);
    public static Animation myself = null;

    // https://stackoverflow.com/questions/25873769/launch-javafx-application-from-another-class
    public static Animation waitForStartUpTest() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return myself;
    }

    public static void setStartUpTest(Animation animationObject) {
        myself = animationObject;
        latch.countDown();
    }

    public Animation() {
        setStartUpTest(this);
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println(this);
        this.txt = new Text();
        this.txt.setText("Coordinator");
        this.txt.setFill(Color.RED);
        this.txt.setFont(Font.font("null", FontWeight.BOLD, 20));
        this.txt.setX(50);
        this.txt.setY(180);
        System.out.println(this.txt);

        Pane root = new Pane();
        root.getChildren().add(this.txt);
//      root.getChildren().add(txt1);
//      root.getChildren().add(r);
//      root.getChildren().add(r2);
//      root.getChildren().add(r3);
//      root.getChildren().add(line);
//      root.getChildren().add(line2);
        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setMaximized(true);
        primaryStage.setTitle("Animation");
        primaryStage.setScene(scene);
        primaryStage.show();
        System.out.println("after show");           
    }

    public void something(int counter){
        System.out.println("hello from something " + counter);
        System.out.println(this.txt.getText());
        this.txt.setText(Integer.toString(counter));
    }
}
//      Rectangle r = new Rectangle();
//      r.setHeight(100);
//      r.setWidth(100);
//      // r.setRotate(0);
//      r.setFill(Color.BLACK);
//      r.setLayoutX(50);
//      r.setLayoutY(320);
// 
// 
//      Line line = new Line();
//      line.setStartX(100);
//      line.setStartY(310);
//      line.setEndX(100);
//      line.setEndY(200);
//      line.setStroke(Color.BLACK);
// 
//      Text txt1 = new Text();
//         txt1.setText("Order Processor");
//         txt1.setFill(Color.RED);
//         txt1.setFont(Font.font("null", FontWeight.BOLD, 20));
//         txt1.setX(800);
//         txt1.setY(180);
//      
//      Rectangle r2 = new Rectangle();
//      r2.setHeight(100);
//      r2.setWidth(100);
//      // r2.setRotate(45);
//      r2.setFill(Color.BLUE);
//      r2.setLayoutX(850);
//      r2.setLayoutY(320);
// 
//      Line line2 = new Line();
//      line2.setStartX(900);
//      line2.setStartY(310);
//      line2.setEndX(900);
//      line2.setEndY(200);
//      line2.setStroke(Color.BLACK);
// 
//      Rectangle r3 = new Rectangle();
//      r3.setHeight(40);
//      r3.setWidth(40);
//      // r3.setRotate(45);
//      r3.setFill(Color.RED);
//      r3.setLayoutX(100);
//      r3.setLayoutY(330);
// 
//      FadeTransition ft1 = new FadeTransition(Duration.millis(1000), r3);
//      ft1.setFromValue(0.0);
//      ft1.setToValue(1.0);
// 
//      TranslateTransition t = new TranslateTransition();
//      t.setDuration(Duration.seconds(1));
//      t.setAutoReverse(true);
//      t.setCycleCount(1);
//      t.setToY(-100);
//      // t.setToX(100);
//      t.setNode(r3);
//      // t.play();
//      
//      TranslateTransition t1 = new TranslateTransition();
//      t1.setDuration(Duration.seconds(1));
//      t1.setAutoReverse(true);
//      t1.setCycleCount(1);
//      t1.setToX(760);
//      t1.setNode(r3);
//      // t1.play();
// 
//      TranslateTransition t2 = new TranslateTransition();
//      t2.setDuration(Duration.seconds(1));
//      t2.setAutoReverse(true);
//      t2.setCycleCount(1);
//      t2.setToY(10);
//      // t2.setToX(100);
//      t2.setNode(r3);
//      // t2.play();
// 
//      // FadeTransition ft = new FadeTransition(Duration.millis(1000), r3);
//      // ft.setFromValue(1.0);
//      // ft.setToValue(0.0);
// 
//      SequentialTransition sequentialTransition = new SequentialTransition();
//      sequentialTransition.getChildren().addAll( ft1, t, t1, t2);
//      sequentialTransition.play();
