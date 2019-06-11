package main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ColorAdjustmentNode
    extends StandardHBox

{
  private Slider slSaturation;
  private Slider slBrightness;
  private CheckBox cbxAutoContrast;
  
  private Button btnReset;
  private MainUi mainUi;
  
  public static boolean defaultAutoContrast = true;
  
  private void setTooltip(final Control control, final String tooltipText)
  {
    Tooltip tooltip = new Tooltip(tooltipText);
    tooltip.setFont(Font.font(null, FontWeight.NORMAL, 18));
    control.setTooltip(tooltip);
    
    control.setOnMouseEntered(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(MouseEvent event)
      {
        // Workaround, damit Tooltip nicht immer kleiner wird (Bug in JavaFX)
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(Font.font(null, FontWeight.NORMAL, 18));
        control.setTooltip(tooltip);
      }
    });
  }
  
  public ColorAdjustmentNode()
  {
    getStyleClass().add("mainimage"); //$NON-NLS-1$
    cbxAutoContrast = new CheckBox(Messages.getString("ColorAdjustmentNode.1")); //$NON-NLS-1$
    cbxAutoContrast.setSelected(true);
    setTooltip(cbxAutoContrast, Messages.getString("ColorAdjustmentNode.2")); //$NON-NLS-1$
    
    cbxAutoContrast.selectedProperty().addListener(new ChangeListener<Boolean>()
    {
      
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
      {
        mainUi.getSelectedMpoFile().setEnhancedContrast(newValue);
        mainUi.reloadMainImage(MPOFile.scalePreview);
      }
    });
    
    slSaturation = makeSlider();
    setTooltip(slSaturation, Messages.getString("ColorAdjustmentNode.3")); //$NON-NLS-1$
    slSaturation.valueProperty().addListener(new ChangeListener<Number>()
    {
      @Override
      public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2)
      {
        mainUi.getSelectedMpoFile().setUserSaturationDiff(arg2.intValue());
        mainUi.reloadMainImage(MPOFile.scalePreview);
      }
    });
    
    slBrightness = makeSlider();
    setTooltip(slBrightness, Messages.getString("ColorAdjustmentNode.4")); //$NON-NLS-1$
    slBrightness.valueProperty().addListener(new ChangeListener<Number>()
    {
      @Override
      public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2)
      {
        mainUi.getSelectedMpoFile().setUserBrithness(arg2.intValue());
        mainUi.reloadMainImage(MPOFile.scalePreview);
      }
    });
    
    EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>()
    {
      
      @Override
      public void handle(ActionEvent arg0)
      {
        setAutoContrast(defaultAutoContrast);
        setBrightness(0);
        setSaturation(0);
        
        MPOFile selectedMpoFile = mainUi.getSelectedMpoFile();
        selectedMpoFile.setUserBrithness(0);
        selectedMpoFile.setEnhancedContrast(defaultAutoContrast);
        selectedMpoFile.setUserSaturationDiff(0);
        mainUi.reloadMainImage(MPOFile.scalePreview);
      }
    };
    
    btnReset = UIHelper.makeButton(handler, Messages.getString("ColorAdjustmentNode.5"), //$NON-NLS-1$
                                   "Command-Reset-256.png"); //$NON-NLS-1$
    
    VBox sliderLabeBox = new VBox();
    Label lblBrigtness = new Label(Messages.getString("ColorAdjustmentNode.7")); //$NON-NLS-1$
    Label lblSaturation = new Label(Messages.getString("ColorAdjustmentNode.8")); //$NON-NLS-1$
    VBox.setMargin(lblSaturation, new Insets(10, 0, 0, 0));
    
    sliderLabeBox.getChildren().addAll(lblBrigtness, lblSaturation);
    
    VBox sliderBox = new VBox();
    sliderBox.getChildren().addAll(slBrightness, slSaturation);
    getChildren().addAll(cbxAutoContrast, sliderLabeBox, sliderBox, btnReset);
  }
  
  private Slider makeSlider()
  {
    final Slider slider = new Slider();
    slider.setMin(-100);
    slider.setMax(100);
    slider.setValue(0);
    slider.setShowTickMarks(true);
    slider.setOnScroll(new EventHandler<ScrollEvent>()
    {
      @Override
      public void handle(ScrollEvent event)
      {
        int diff = (int) (event.getDeltaY() / 3);
        
        if (diff > 3)
          diff = 3;
        
        if (diff < -3)
          diff = -3;
        
        slider.setValue(slider.getValue() + diff);
      }
    });
    return slider;
  }
  
  public void setSaturation(final int saturation)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        slSaturation.setValue(saturation);
      }
    });
  }
  
  public void setBrightness(final int brightness)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        slBrightness.setValue(brightness);
      }
    });
  }
  
  public void setAutoContrast(final boolean autoContrast)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        cbxAutoContrast.setSelected(autoContrast);
      }
    });
  }
  
  public void setMainUi(MainUi mainUi)
  {
    this.mainUi = mainUi;
    
  }
}
