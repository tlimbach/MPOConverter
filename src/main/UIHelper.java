package main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class UIHelper
{
  
  final public static int BUTTON_HEIGHT = 46;
  final public static int BUTTON_WIDTH = 46;
  
  final public static int DIALOG_BUTTON_WIDTH = 60;
  final public static int DIALOG_BUTTON_HEIGHT = (int) (DIALOG_BUTTON_WIDTH * (2D / 3D));
  
  public static BufferedImage toBufferedImage(Image img)
  {
    if (img instanceof BufferedImage)
    {
      return (BufferedImage) img;
    }
    
    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    
    Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(img, 0, 0, null);
    bGr.dispose();
    
    return bimage;
  }
  
  public static javafx.scene.image.Image getScaled(javafx.scene.image.Image image, int width, int height)
  {
    BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
    Image scaledInstance = bImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    
    return SwingFXUtils.toFXImage(UIHelper.toBufferedImage(scaledInstance), null);
    
  }
  
  static boolean messageAnswer = false;
  
  private static Button makeDialogButton(String text)
  {
    Button btn = new Button(text);
    btn.setMinSize(DIALOG_BUTTON_WIDTH, DIALOG_BUTTON_HEIGHT);
    btn.setMaxSize(DIALOG_BUTTON_WIDTH, DIALOG_BUTTON_HEIGHT);
    btn.setPrefSize(DIALOG_BUTTON_WIDTH, DIALOG_BUTTON_HEIGHT);
    return btn;
  }
  
  public static ShadowButton makeButton(EventHandler handler, String name, String image)
  {
    return new ShadowButton(handler, name, image);
  }
  
  public static ToggleButton makeToggleButton(String text, String image)
  {
    return new ShadowToggleButton(text, image);
  }
  
  public static Label getFillLabel(int space)
  {
    String emtpy = "                                                   "; //$NON-NLS-1$
    Label lbl = new Label(emtpy.substring(0, space));
    return lbl;
    
  }
  
  public static boolean showMessage(String message, boolean confirm)
  {
    messageAnswer = false;
    final Stage dialog = new Stage();
    dialog.setTitle("MPO Converter"); //$NON-NLS-1$
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initStyle(StageStyle.UTILITY);
    BorderPane pane = new BorderPane();
    Scene scene = new Scene(pane);
    dialog.setResizable(false);
    HBox boxLabelImage = new HBox(30);
    BorderPane.setMargin(boxLabelImage, new Insets(40, 40, 70, 40));
    Label label = new Label(message);
    ImageView image = new ImageView(getScaled(new javafx.scene.image.Image(UIHelper.class.getResourceAsStream("images/" //$NON-NLS-1$
                                                                                                              + (confirm ? "qm.png" : "im.png"))), 40, 40)); //$NON-NLS-1$ //$NON-NLS-2$
    
    boxLabelImage.getChildren().addAll(label, image);
    label.setFont(Font.font(null, FontWeight.NORMAL, 16));
    HBox topArea = new HBox();
    topArea.setMinHeight(30);
    // topArea.setStyle("-fx-background-color: ghostwhite; -fx-text-fill: white;");
    topArea.setStyle("-fx-background-color: ghostwhite; -fx-text-fill: white; -fx-border-insets: 0 0 0 0; -fx-border-color: -fx-text-box-border;  -fx-border-width: 0 0 1 0;"); //$NON-NLS-1$
    Button btnOk = makeDialogButton(Messages.getString(confirm ? "UIHelper.6" : "UIHelper.8")); //$NON-NLS-1$
    btnOk.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
    Button btnCancel = makeDialogButton(Messages.getString("UIHelper.7")); //$NON-NLS-1$
    btnCancel.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
    HBox boxButtons = new HBox(20);
    HBox.setMargin(btnOk, new Insets(20, confirm ? 0 : 40, 20, 0));
    HBox.setMargin(btnCancel, new Insets(20, 40, 20, 0));
    boxButtons.setStyle("-fx-background-color: ghostwhite; -fx-text-fill: white;-fx-content-display: top; -fx-border-insets: 0 0 0 0; -fx-border-color: -fx-text-box-border;  -fx-border-width: 1 0 0 0;"); //$NON-NLS-1$
    boxButtons.setAlignment(Pos.CENTER_RIGHT);
    
    if (confirm)
      boxButtons.getChildren().addAll(btnOk, btnCancel);
    else
      boxButtons.getChildren().add(btnOk);
    
    pane.setTop(topArea);
    pane.setCenter(boxLabelImage);
    pane.setBottom(boxButtons);
    
    btnOk.setOnAction(new EventHandler<ActionEvent>()
    {
      @Override
      public void handle(ActionEvent event)
      {
        dialog.close();
        messageAnswer = true;
      }
    });
    
    btnCancel.setOnAction(new EventHandler<ActionEvent>()
    {
      @Override
      public void handle(ActionEvent event)
      {
        dialog.close();
        messageAnswer = false;
      }
    });
    
    dialog.setScene(scene);
    dialog.showAndWait();
    
    return messageAnswer;
  }
  
  public static ShadowButton makeButton(EventHandler<ActionEvent> handler, String name)
  {
    return makeButton(handler, name, null);
  }
  
  public static void skinControl(Control control, String tooltipText)
  {
    if (tooltipText != null)
    {
      Tooltip tooltip = new Tooltip(tooltipText);
      tooltip.setFont(Font.font(null, FontWeight.NORMAL, 18));
      control.setTooltip(tooltip);
    }
    
    control.setMinSize(UIHelper.BUTTON_WIDTH, UIHelper.BUTTON_HEIGHT);
    control.setMaxSize(UIHelper.BUTTON_WIDTH, UIHelper.BUTTON_HEIGHT);
    control.setPrefSize(UIHelper.BUTTON_WIDTH, UIHelper.BUTTON_HEIGHT);
    DropShadow dropShadow = new DropShadow();
    dropShadow.setRadius(3.0);
    dropShadow.setOffsetX(2.0);
    dropShadow.setOffsetY(2.0);
    dropShadow.setBlurType(BlurType.GAUSSIAN);
    double gray = 0.4D;
    dropShadow.setColor(Color.color(gray, gray, gray));
    control.setEffect(dropShadow);
  }
  
}
