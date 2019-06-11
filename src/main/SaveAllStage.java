package main;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SaveAllStage
    extends Stage
{
  private int totalFiles;
  private int currentFile;
  private ProgressBar pb;
  private Label lblText;
  
  public SaveAllStage()
  {
    pb = new ProgressBar(0.0);
    HBox box = new HBox(20D);
    lblText = new Label("(100/100)");
    box.getChildren().addAll(pb, lblText);
    BorderPane root = new BorderPane(box);
    setResizable(false);
    setScene(new Scene(root));
    initModality(Modality.APPLICATION_MODAL);
    show();
  }
  
  public void setTotalFiles(int numberFiles)
  {
    this.totalFiles = numberFiles;
  }
  
  public void setCurrentFile(final int curfile)
  {
    this.currentFile = curfile;
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        pb.setProgress((double) curfile / (double) totalFiles);
        lblText.setText(" (" + curfile + "/" + totalFiles + ")");
      }
    });
  }
}
