package main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialog
{
  
  private Stage dialog;
  private ProgressBar pBar;
  private int maxValue;
  private Label label;
  private String message;
  
  interface ICancelledListener
  {
    public void jobCancelled();
  }
  
  public ProgressDialog(final String message, final int max, final ICancelledListener listener)
  {
    this.message = message;
    this.maxValue = max;
    Platform.runLater(new Runnable()
    {
      
      @Override
      public void run()
      {
        dialog = new Stage();
        dialog.setTitle("MPO Converter");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane);
        dialog.setResizable(false);
        VBox boxLabelImage = new VBox(30);
        BorderPane.setMargin(boxLabelImage, new Insets(40, 40, 70, 40));
        label = new Label(message);
        pBar = max > 0 ? new ProgressBar(max) : new ProgressBar();
        if (max > 0)
        {
          pBar.setProgress(0.0001);
        }
        pBar.setMinWidth(300);
        boxLabelImage.getChildren().addAll(label, pBar);
        label.setFont(Font.font(null, FontWeight.NORMAL, 16));
        HBox topArea = new HBox();
        topArea.setMinHeight(30);
        topArea.setStyle("-fx-background-color: ghostwhite; -fx-text-fill: white; -fx-border-insets: 0 0 0 0; -fx-border-color: -fx-text-box-border;  -fx-border-width: 0 0 1 0;");
        Button btnOk = new Button("Abbrechen");
        pane.setTop(topArea);
        pane.setCenter(boxLabelImage);
        BorderPane.setAlignment(btnOk, Pos.CENTER);
        BorderPane.setMargin(btnOk, new Insets(0, 0, 50, 0));
        
        if (listener != null)
          pane.setBottom(btnOk);
        
        btnOk.setOnAction(new EventHandler<ActionEvent>()
        {
          @Override
          public void handle(ActionEvent event)
          {
            dialog.close();
            listener.jobCancelled();
          }
        });
        
        dialog.setScene(scene);
        dialog.show();
      }
    });
    
  }
  
  public void close()
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        dialog.close();
      }
    });
    
  }
  
  public void setProgress(final int currentValue)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        pBar.setProgress((double) currentValue / (double) maxValue);
        label.setText(message + "   (" + currentValue + "/" + maxValue + ")");
      }
    });
    
  }
}
