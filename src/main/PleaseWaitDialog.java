package main;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PleaseWaitDialog
    extends Stage
{
  private ProgressBar pb;
  private Label lblText;
  
  public PleaseWaitDialog(String waitText, final Runnable run)
  {
    pb = new ProgressBar();
    HBox box = new HBox(20D);
    lblText = new Label(waitText);
    box.getChildren().addAll(pb, lblText);
    BorderPane root = new BorderPane(box);
    setResizable(false);
    setScene(new Scene(root));
    initModality(Modality.APPLICATION_MODAL);
    show();
    
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        run.run();
        Platform.runLater(new Runnable()
        {
          @Override
          public void run()
          {
            PleaseWaitDialog.this.close();
          }
        });
      }
    }).start();
    
  }
}
