package main;

import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ShadowToggleButton
    extends ToggleButton
    implements IShadowButton
{
  
  private String tooltipText;
  
  public ShadowToggleButton(String tooltipText, String imageName)
  {
    
    setOnMouseEntered(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(MouseEvent event)
      {
        // Workaround, damit Tooltip nicht immer kleiner wird (Bug in JavaFX)
        UIHelper.skinControl(ShadowToggleButton.this, ShadowToggleButton.this.tooltipText);
      }
    });
    
    this.tooltipText = tooltipText;
    UIHelper.skinControl(this, tooltipText);
    
    {
      Image image = null;
      try
      {
        image = new Image(UIHelper.class.getResourceAsStream("images/" + imageName));
      }
      catch (Exception e)
      {
        System.err.println("Can not load " + imageName);
        e.printStackTrace();
      }
      
      Image scaled = UIHelper.getScaled(image, 35, 25);
      
      ImageView imageView = new ImageView(scaled);
      setGraphic(imageView);
      
    }
  }
  
  @Override
  public void setDisabledShadowButton(boolean disable)
  {
    setDisable(disable);
    
    if (disable)
      setEffect(null);
    else
    {
      UIHelper.skinControl(this, tooltipText);
    }
  }
  
}
