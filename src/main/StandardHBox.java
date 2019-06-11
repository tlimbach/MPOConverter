package main;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

public class StandardHBox
    extends HBox
{
  public StandardHBox()
  {
    super();
    setSpacing(5);
    setPadding(new Insets(5, 10, 5, 10));
  }
}
