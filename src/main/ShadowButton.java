package main;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ShadowButton
    extends Button
    implements IShadowButton
{
  
  private String tooltipText;
  
  public ShadowButton(EventHandler handler, String tooltipText, String imageName)
  {
    setOnMouseEntered(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(MouseEvent event)
      {
        // Workaround, damit Tooltip nicht immer kleiner wird (Bug in JavaFX)
        UIHelper.skinControl(ShadowButton.this, ShadowButton.this.tooltipText);
      }
    });
    
    this.tooltipText = tooltipText;
    UIHelper.skinControl(this, tooltipText);
    setOnAction(handler);
    setAlignment(Pos.CENTER);
    if (imageName != null)
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
      
      int imageSize = 30;
      Color imageColor = Color.GOLDENROD;
      
      Image scaled = UIHelper.getScaled(image, imageSize, imageSize);
      
      scaled = recolor(scaled, imageColor);
      
      ImageView imageView = new ImageView(scaled);
      setGraphic(imageView);
      
    }
    else
    {
      setText(tooltipText);
    }
    
  }
  
  public static int rgba(Color color)
  {
    int red = (int) (color.getRed() * 255D);
    int green = (int) (color.getGreen() * 255D);
    int blue = (int) (color.getBlue() * 255D);
    
    int rgba = 255;
    rgba = (rgba << 8) + red;
    rgba = (rgba << 8) + green;
    rgba = (rgba << 8) + blue;
    
    return rgba;
  }
  
  private Image recolor(Image image, Color color)
  {
    BufferedImage originalImage = SwingFXUtils.fromFXImage(image, null);
    int width = originalImage.getWidth();
    int height = originalImage.getHeight();
    
    BufferedImage coloredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int x = 0; x < width; x++)
    {
      for (int y = 0; y < height; y++)
      {
        int origRgb = originalImage.getRGB(x, y);
        
        if ((origRgb & 0xff000000) == 0xff000000)
        {
          coloredImage.setRGB(x, y, rgba(color));
        }
      }
    }
    
    return SwingFXUtils.toFXImage(coloredImage, null);
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
