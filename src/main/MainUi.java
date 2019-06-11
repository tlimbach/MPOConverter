package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.ProgressDialog.ICancelledListener;

public class MainUi
    implements EventHandler<ActionEvent>
{
  
  private MPOFile selectedMPO;
  
  ToggleButton btn2d = UIHelper.makeToggleButton("2D", "2d.png"); //$NON-NLS-1$ //$NON-NLS-2$
  ToggleButton btn3d = UIHelper.makeToggleButton("3D", "3d.png"); //$NON-NLS-1$ //$NON-NLS-2$
  Button btnSaveSelected = makeButton(Messages.getString("MainUi.10"), "Save-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  // Button btnSaveSelected3D = makeButton("Speichern (3D)");
  Button btnNextImage = makeButton(Messages.getString("MainUi.3"), "Arrowhead-Right-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  Button btnPrevImage = makeButton(Messages.getString("MainUi.5"), "Arrowhead-Left-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  Button btnSaveAll = makeButton(Messages.getString("MainUi.11"), "Save As-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  Button btnOpen = makeButton(Messages.getString("MainUi.61"), "Folder-Open-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  // Button btnSaveAll3D = makeButton("Alle Speichern (3D)");
  Button btnResetZoom = makeButton(Messages.getString("MainUi.12"), "Full-Screen-Expand-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  Button btnDeleteFromDisk = makeButton(Messages.getString("MainUi.13"), "Delete-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  Button btnRemoveFromList = makeButton(Messages.getString("MainUi.14"), "Delete-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  private Button btnAbout = makeButton(Messages.getString("MainUi.19"), "Dialog-Box-About-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  private Button btnHelp = makeButton(Messages.getString("MainUi.21"), "Help-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
  
  private ColorAdjustmentNode boxColorAdjustment = new ColorAdjustmentNode();
  
  private Node[] nodesVisibleWhenImageLoadedOnly = { btn2d,
                                                    btn3d,
                                                    btnSaveAll,
                                                    btnSaveSelected,
                                                    btnDeleteFromDisk,
                                                    btnRemoveFromList,
                                                    boxColorAdjustment,
                                                    btnNextImage,
                                                    btnPrevImage };
  
  ObservableList<String> observableMpoList = FXCollections.observableArrayList(new ArrayList<String>());
  
  private Node selectedNode;
  private Stage stage;
  protected int lastMonitorScale = -1;
  
  private int nextDrawScale = -1;
  boolean doesReload = false;
  
  private MainImagePane imagePane;
  
  Thread loadHiresThread = null;
  private VBox boxImages;
  private BorderPane main;
  private BorderPane paneRight;
  private ParallaxNode paralaxElements;
  private ScrollPane imageScrollPane;
  
  private BorderPane paneLabelNoImages;
  
  public MainUi(Stage stage)
  {
    this.stage = stage;
    initGui();
    
    observableMpoList.addListener(new ListChangeListener<String>()
    {
      
      @Override
      public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c)
      {
        System.out.println("someone added somethung " + observableMpoList.size()); //$NON-NLS-1$
        
        if (observableMpoList.size() == 0)
        {
          setButtonStates(false);
        }
        
      }
    });
    
    new Timer().schedule(new TimerTask()
    {
      
      @Override
      public void run()
      {
        if (nextDrawScale != -1 && !doesReload)
        {
          reloadMainImage(nextDrawScale);
        }
      }
    }, 10, 10);
  }
  
  private Button makeButton(String string)
  {
    return makeButton(string, null);
  }
  
  private ShadowButton makeButton(String text, String imageName)
  {
    return UIHelper.makeButton(this, text, imageName);
  }
  
  public void centerNodeInScrollPane(final ScrollPane scrollPane, final Node node)
  {
    try
    {
      double h = scrollPane.getContent().getBoundsInLocal().getHeight();
      double y = (node.getBoundsInParent().getMaxY() + node.getBoundsInParent().getMinY()) / 2.0;
      double v = scrollPane.getViewportBounds().getHeight();
      final double end = scrollPane.getVmax() * ((y - 0.5 * v) / (h - v));
      
      Platform.runLater(new Runnable()
      {
        @Override
        public void run()
        {
          scrollPane.setVvalue(end);
        }
      });
    }
    catch (Exception e)
    {
      // dont care
    }
  }
  
  private void installEventHandler(final Node keyNode)
  {
    final EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>()
    {
      @Override
      public void handle(final KeyEvent keyEvent)
      {
        String name = keyEvent.getCode().getName();
        if (name.equals("Down") || name.equals("Up")) //$NON-NLS-1$ //$NON-NLS-2$
        {
          boolean directionDown = name.equals("Down"); //$NON-NLS-1$
          gotoNextImage(directionDown);
        }
        keyEvent.consume();
      }
    };
    
    keyNode.setOnKeyTyped(keyEventHandler);
    keyNode.setOnKeyReleased(keyEventHandler);
  }
  
  private void initGui()
  {
    stage.setTitle(Messages.getString("MainUi.6")); //$NON-NLS-1$
    Image image = new Image(UIHelper.class.getResourceAsStream("images/icon_256x256.png")); //$NON-NLS-1$
    stage.getIcons().add(image);
    main = new BorderPane();
    
    Label lblNoImages = new Label(Messages.getString("MainUi.7")); //$NON-NLS-1$
    lblNoImages.setAlignment(Pos.CENTER);
    lblNoImages.setFont(Font.font(null, FontWeight.BOLD, 36));
    lblNoImages.setId("label_no_images"); //$NON-NLS-1$
    lblNoImages.setMinSize(300, 300);
    
    final BorderPane splitPane = new BorderPane();
    
    paneRight = new BorderPane();
    paneLabelNoImages = new BorderPane(lblNoImages);
    paneRight.setCenter(paneLabelNoImages);
    paneLabelNoImages.getStyleClass().add("mainimage"); //$NON-NLS-1$
    paneLabelNoImages.getStyleClass().add("main"); //$NON-NLS-1$
    
    BorderPane buttonPane = new BorderPane();
    buttonPane.getStyleClass().add("nebenrollen"); //$NON-NLS-1$
    HBox leftButtonBar = new StandardHBox();
    HBox rightButtonBar = new StandardHBox();
    
    rightButtonBar.getChildren().addAll(btnHelp, btnAbout);
    final ToggleGroup group2D3D = new ToggleGroup();
    
    boxColorAdjustment.setMainUi(this);
    boxColorAdjustment.setDisable(true);
    
    btn2d.setToggleGroup(group2D3D);
    btn3d.setToggleGroup(group2D3D);
    btn3d.setSelected(true);
    btn2d.setOnAction(this);
    btn3d.setOnAction(this);
    
    leftButtonBar.getChildren().addAll(btnPrevImage,
                                       btnNextImage,
                                       UIHelper.getFillLabel(8),
                                       btn2d,
                                       btn3d,
                                       UIHelper.getFillLabel(8),
                                       btnOpen,
                                       UIHelper.getFillLabel(3),
                                       btnSaveAll);
    buttonPane.setLeft(leftButtonBar);
    buttonPane.setRight(rightButtonBar);
    main.setTop(buttonPane);
    
    BorderPane paneLeft = new BorderPane();
    splitPane.setLeft(paneLeft);
    splitPane.setCenter(paneRight);
    
    boxImages = new VBox();
    boxImages.setSpacing(5);
    boxImages.getStyleClass().add("scrollpane"); //$NON-NLS-1$
    imageScrollPane = new ScrollPane(boxImages);
    imageScrollPane.getStyleClass().add("scrollpane"); //$NON-NLS-1$
    imageScrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
    
    paneLeft.setCenter(imageScrollPane);
    paneLeft.getStyleClass().add("scrollpane"); //$NON-NLS-1$
    paneLeft.setMinWidth(230); // breite der linken Pane
    
    group2D3D.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
    {
      @Override
      public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
      {
        reloadMainImage(MPOFile.scalePreview);
      }
    });
    
    imagePane = new MainImagePane(this, main);
    main.setCenter(splitPane);
    
    paralaxElements = new ParallaxNode(this);
    disable(!btn3d.isSelected(), paralaxElements.getChildren().toArray(new Node[paralaxElements.getChildren().size()]));
    
    disable(true, btnResetZoom);
    
    HBox buttonsBottom = new HBox(20);
    buttonsBottom.getStyleClass().add("nebenrollen"); //$NON-NLS-1$
    HBox buttonsRight = new HBox(20);
    HBox buttonsMiddle = new StandardHBox();
    
    buttonsMiddle.getChildren().addAll(btnSaveSelected /*
                                                       * ,
                                                       * btnDeleteFromDisk
                                                       */, btnRemoveFromList, btnResetZoom);
    
    buttonsRight.getChildren().addAll(boxColorAdjustment, paralaxElements);
    buttonsRight.getStyleClass().add("mainimage"); //$NON-NLS-1$
    buttonsBottom.getChildren().addAll(buttonsMiddle, buttonsRight);
    paneRight.setBottom(buttonsBottom);
    
    EventHandler<DragEvent> dragOverEventHandler = getDragOverEventhandler();
    EventHandler<DragEvent> dragDroppedEventhandler = getDragDropEventhandler();
    
    paneRight.setOnDragOver(dragOverEventHandler);
    paneRight.setOnDragDropped(dragDroppedEventhandler);
    
    final Scene scene = new Scene(main);
    
    scene.getStylesheets().add("stylesheet.css"); //$NON-NLS-1$
    
    stage.setScene(scene);
    
    stage.setOnCloseRequest(new EventHandler<WindowEvent>()
    {
      @Override
      public void handle(WindowEvent event)
      {
        System.exit(1);
      }
    });
    
    installEventHandler(paneLeft);
    setButtonStates(false);
    
    stage.setWidth(1200);
    stage.setHeight(750);
    
    stage.setMinHeight(stage.getHeight());
    stage.setMinWidth(stage.getWidth());
    stage.show();
    
  }
  
  private EventHandler<DragEvent> getDragDropEventhandler()
  {
    return new EventHandler<DragEvent>()
    {
      
      @Override
      public void handle(DragEvent event)
      {
        Dragboard dragboard = event.getDragboard();
        
        List<File> filesFromDir = dragboard.getFiles();
        fillImages(filesFromDir);
        
      }
    };
  }
  
  private EventHandler<DragEvent> getDragOverEventhandler()
  {
    return new EventHandler<DragEvent>()
    {
      @Override
      public void handle(DragEvent event)
      {
        event.acceptTransferModes(event.getDragboard().hasFiles() ? TransferMode.ANY : TransferMode.NONE);
      }
    };
  }
  
  public void reloadMainImage(final int previewScale)
  {
    if (boxImages.getChildren().size() == 0)
    {
      System.out.println("1"); //$NON-NLS-1$
      nextDrawScale = -1;
      doesReload = false;
      return;
    }
    
    if (doesReload)
    {
      System.out.println("2"); //$NON-NLS-1$
      nextDrawScale = previewScale;
      return;
    }
    doesReload = true;
    nextDrawScale = -1;
    
    if (loadHiresThread != null)
    {
      loadHiresThread.stop();
    }
    
    Thread loadImageThread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          int imageScale = previewScale;
          imagePane.setImage(btn2d.isSelected() ? selectedMPO.get2DImage(imageScale)
                                               : selectedMPO.get3DImage(imageScale, false));
          
          boxColorAdjustment.setSaturation(selectedMPO.getUserSaturationDiff());
          boxColorAdjustment.setBrightness(selectedMPO.getUserBrightnessDiff());
          boxColorAdjustment.setAutoContrast(selectedMPO.getUserAutoContrast());
          
          disable(!selectedMPO.isZoomed(), btnResetZoom);
          
          disable(btn2d.isSelected(),
                  paralaxElements.getChildren().toArray(new Node[paralaxElements.getChildren().size()]));
          
          disable(false, nodesVisibleWhenImageLoadedOnly);
          disable(false, boxColorAdjustment.getChildren().toArray(new Node[boxColorAdjustment.getChildren().size()]));
          
          doesReload = false;
          loadHiresThread = new Thread(new Runnable()
          {
            @Override
            public void run()
            {
              // setCursorWait(true);
              Image image = null;//
              try
              {
                image = btn2d.isSelected() ? selectedMPO.get2DImage(MPOFile.scaleMonitor)
                                          : selectedMPO.get3DImage(MPOFile.scaleMonitor, false);
                imagePane.setImage(image);
                selectedMPO.get3DImage(MPOFile.scaleParallaxeShift, false); // "Preload"
              }
              catch (FileWasDeletedException e)
              {
                showNoMoreMPOFileDialog();
                System.err.println("File " + selectedMPO.getFilename() + " already deleted!"); //$NON-NLS-1$ //$NON-NLS-2$
                doesReload = false;
                deleteFile(false);
                selectedMPO.resetZoom();
              }
            }
          });
          
          loadHiresThread.start();
        }
        catch (FileWasDeletedException e)
        {
          showNoMoreMPOFileDialog();
          doesReload = false;
          deleteFile(false);
          selectedMPO.resetZoom();
        }
        catch (Exception e)
        {
          System.out.println("Error decoding..."); //$NON-NLS-1$
          e.printStackTrace();
          doesReload = false;
          selectedMPO.resetZoom();
        }
        
        Platform.runLater(new Runnable()
        {
          @Override
          public void run()
          {
            if (paneRight.getCenter() != imagePane && observableMpoList.size() > 0)
              paneRight.setCenter(imagePane);
          }
        });
        
      }
    });
    
    loadImageThread.start();
  }
  
  protected void showNoMoreMPOFileDialog()
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        UIHelper.showMessage(Messages.getString("MainUi.40") //$NON-NLS-1$
                             + selectedMPO.getFilename()
                             + Messages.getString("MainUi.41"), false); //$NON-NLS-1$
      }
    });
    
  }
  
  private void setCursorWait(final boolean wait)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        main.setCursor(wait ? Cursor.WAIT : Cursor.DEFAULT);
      }
    });
  }
  
  @Override
  public void handle(ActionEvent event)
  {
    final Object source = event.getSource();
    
    if (source == btnOpen)
    {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle(Messages.getString("MainUi.43")); //$NON-NLS-1$
      fillImages(fileChooser.showOpenMultipleDialog(stage));
    }
    
    if (source == btn2d)
    {
      btn3d.setSelected(!btn2d.isSelected());
    }
    if (source == btn3d)
    {
      btn2d.setSelected(!btn3d.isSelected());
    }
    
    if (source == btnSaveSelected)
    {
      if (selectedMPO != null)
      {
        final File outputFile = selectedMPO.getOutputFile(btn3d.isSelected());
        
        if (outputFile.exists())
        {
          boolean overrideExisting = UIHelper.showMessage(Messages.getString("MainUi.44") //$NON-NLS-1$
                                                          + outputFile.getName()
                                                          + Messages.getString("MainUi.45"), true); //$NON-NLS-1$
          
          if (!overrideExisting)
            return;
        }
        
        new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            final StringBuffer msg = new StringBuffer(Messages.getString("MainUi.46") + " (" + outputFile.getName() + ")"); //$NON-NLS-1$
            final ProgressDialog progressDialog = new ProgressDialog(Messages.getString("MainUi.47"), 0, null); //$NON-NLS-1$
            
            try
            {
              
              selectedMPO.writeImageToDisk(btn3d.isSelected(), outputFile);
            }
            catch (FileWasDeletedException e)
            {
              msg.delete(0, msg.length());
              msg.append(Messages.getString("MainUi.48")); //$NON-NLS-1$
              deleteFile(false);
            }
            
            progressDialog.close();
            
            Platform.runLater(new Runnable()
            {
              @Override
              public void run()
              {
                UIHelper.showMessage(msg.toString(), false);
              }
            });
            
          }
        }).start();
        
      }
    }
    
    if (source == btnResetZoom)
    {
      selectedMPO.resetZoom();
      reloadMainImage(MPOFile.scaleMonitor);
    }
    
    if (source == btnSaveAll)
    {
      final ObservableList<Node> children = boxImages.getChildren();
      final StringBuffer cancelled = new StringBuffer();
      
      for (Node node : children)
      {
        ImageView view = (ImageView) node.getUserData();
        selectedMPO = (MPOFile) view.getUserData();
        File outputFile = selectedMPO.getOutputFile(btn3d.isSelected());
        if (outputFile.exists())
        {
          boolean overrideExisting = UIHelper.showMessage(Messages.getString("MainUi.49"), //$NON-NLS-1$
                                                          true);
          
          if (!overrideExisting)
            return;
          
          if (overrideExisting)
            break;
        }
        
      }
      
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          
          final ProgressDialog progressDialog = new ProgressDialog(Messages.getString("MainUi.50"), //$NON-NLS-1$
                                                                   children.size(),
                                                                   new ICancelledListener()
                                                                   {
                                                                     
                                                                     @Override
                                                                     public void jobCancelled()
                                                                     {
                                                                       cancelled.append("so siehts aus. User hats abgebrochen!"); //$NON-NLS-1$
                                                                     }
                                                                   });
          
          int i = 0;
          try
          {
            for (Node node : children)
            {
              
              if (cancelled.length() > 0)
                break;
              
              i++;
              progressDialog.setProgress(i);
              ImageView view = (ImageView) node.getUserData();
              
              if (selectedMPO != null)
                selectedMPO.deleteTempData();
              
              selectedMPO = (MPOFile) view.getUserData();
              selectedMPO.writeImageToDisk(btn3d.isSelected(), selectedMPO.getOutputFile(btn3d.isSelected()));
            }
          }
          catch (FileWasDeletedException e)
          {
            cancelled.append("so gehts nicht weiter!"); //$NON-NLS-1$
            Platform.runLater(new Runnable()
            {
              @Override
              public void run()
              {
                progressDialog.close();
                UIHelper.showMessage(Messages.getString("MainUi.53") //$NON-NLS-1$
                                     + selectedMPO.getFilename()
                                     + Messages.getString("MainUi.54"), false); //$NON-NLS-1$
              }
            });
            deleteFile(false);
            return;
          }
          catch (Exception e)
          {
            // Abfangen wenn Export abgebrochen wird und gleich ein
            // Bild entfernt wird (ConcurrentModificationException)
            e.printStackTrace();
          }
          progressDialog.close();
          
          Platform.runLater(new Runnable()
          {
            @Override
            public void run()
            {
              UIHelper.showMessage(cancelled.length() > 0 ? Messages.getString("MainUi.55") : Messages.getString("MainUi.56"), //$NON-NLS-1$ //$NON-NLS-2$
                                   false);
            }
          });
        }
      }).start();
    }
    
    if (source == btnDeleteFromDisk)
    {
      boolean response = UIHelper.showMessage(Messages.getString("MainUi.57"), true); //$NON-NLS-1$
      
      if (response)
        deleteFile(true);
    }
    
    if (source == btnRemoveFromList)
    {
      deleteFile(false);
    }
    
    if (source == btnAbout)
    {
      UIHelper.showMessage(Messages.getString("MainUi.58"), false); //$NON-NLS-1$
    }
    
    if (source == btnNextImage || source == btnPrevImage)
    {
      gotoNextImage(source == btnNextImage);
    }
    
    if (source == btnHelp)
    {
      new HelpDialog();
    }
    
  }
  
  private void deleteFile(final boolean deleteFromDisk)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        Node prevNode = null;
        
        Node[] array = boxImages.getChildren().toArray(new Node[boxImages.getChildren().size()]);
        
        for (int t = 0; t < array.length; t++)
        {
          if (array[t] == selectedNode)
          {
            if (t + 1 < array.length)
            {
              prevNode = array[t + 1];
            }
            else if (array.length > 1)
            {
              prevNode = array[t - 1];
            }
            else
            {
              paneRight.setCenter(paneLabelNoImages);
            }
          }
        }
        
        // File löschen
        ImageView iView = (ImageView) selectedNode.getUserData();
        final MPOFile file = (MPOFile) iView.getUserData();
        
        boxImages.getChildren().remove(selectedNode);
        
        if (prevNode != null)
        {
          selectedNode = prevNode;
          selectedNode.getStyleClass().add("selected_thumbnail"); //$NON-NLS-1$
          ImageView newView = (ImageView) prevNode.getUserData();
          
          if (selectedMPO != null)
            selectedMPO.deleteTempData();
          
          selectedMPO = (MPOFile) newView.getUserData();
          reloadMainImage(MPOFile.scalePreview);
        }
        
        observableMpoList.remove(file.getFilename());
        
        if (deleteFromDisk)
          file.delete();
      }
    });
    
  }
  
  private void setButtonStates(final boolean imageVisible)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        disable(!imageVisible, nodesVisibleWhenImageLoadedOnly);
        disable(!imageVisible, paralaxElements.getChildren().toArray(new Node[paralaxElements.getChildren().size()]));
        disable(!imageVisible,
                boxColorAdjustment.getChildren().toArray(new Node[boxColorAdjustment.getChildren().size()]));
      }
      
    });
    
  }
  
  private void disable(final boolean disable, final Node... nodes)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        for (Node node : nodes)
        {
          node.setDisable(disable);
          
          if (node instanceof IShadowButton)
          {
            IShadowButton sb = (IShadowButton) node;
            
            if (observableMpoList.size() == 0)
              sb.setDisabledShadowButton(true);
            else
              sb.setDisabledShadowButton(disable);
            
          }
        }
      }
    });
  }
  
  public MPOFile getSelectedMpoFile()
  {
    return selectedMPO;
  }
  
  private void computeScales()
  {
    int imageWidth = (int) paneRight.getCenter().getLayoutBounds().getWidth();
    int mpoOriginalWidth = selectedMPO.getMpoOriginalWidth();
    
    MPOFile.scaleHiRes = 1;
    MPOFile.scaleMonitor = Math.max(mpoOriginalWidth / imageWidth, 1);
    
    // Ausreißer verhindern
    if (MPOFile.scaleMonitor > 3)
      MPOFile.scaleMonitor = 2;
    
    if (selectedMPO.isZoomed() && MPOFile.scaleMonitor > 1)
      MPOFile.scaleMonitor--;
    
    MPOFile.scalePreview = MPOFile.scaleMonitor + 2;
    MPOFile.scaleParallaxeShift = MPOFile.scaleMonitor + 3;
  }
  
  private void gotoNextImage(boolean directionDown)
  {
    int itemPos = -1;
    Node[] array = boxImages.getChildren().toArray(new Node[boxImages.getChildren().size()]);
    Node newSelectedNode = null;
    for (int t = 0; t < array.length; t++)
    {
      if (array[t] == selectedNode)
      {
        itemPos = t;
        if (directionDown && t + 1 < array.length) //$NON-NLS-1$
        {
          newSelectedNode = array[t + 1];
        }
        else if (!directionDown && array.length > 1 && t > 0) //$NON-NLS-1$
        {
          newSelectedNode = array[t - 1];
        }
      }
    }
    
    if (newSelectedNode != null)
    {
      selectedNode.getStyleClass().remove("selected_thumbnail"); //$NON-NLS-1$
      selectedNode = newSelectedNode;
      selectedNode.getStyleClass().add("selected_thumbnail"); //$NON-NLS-1$
      ImageView newView = (ImageView) newSelectedNode.getUserData();
      
      if (selectedMPO != null)
        selectedMPO.deleteTempData();
      
      selectedMPO = (MPOFile) newView.getUserData();
      
      reloadMainImage(MPOFile.scalePreview);
    }
    
    // System.out.println("total / itempos = " + array.length + "/" +
    // itemPos);
    
    int minDistance = 3;
    boolean n = array.length > minDistance && itemPos > minDistance && itemPos < (array.length - minDistance);
    
    if (n)
      centerNodeInScrollPane(imageScrollPane, selectedNode);
  }
  
  private void addFiles(final List<File> files)
  {
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        for (File newFile : files)
        {
          
          if (!newFile.getName().toLowerCase().endsWith("mpo")) //$NON-NLS-1$
          {
            continue;
          }
          
          // Keine doppelten Einträge zulassen
          if (observableMpoList.contains(newFile.getName()))
          {
            continue;
          }
          
          final int max = 150;
          if (observableMpoList.size() > max)
          {
            Platform.runLater(new Runnable()
            {
              @Override
              public void run()
              {
                UIHelper.showMessage(Messages.getString("MainUi.59") + max + Messages.getString("MainUi.60"), false); //$NON-NLS-1$ //$NON-NLS-2$
              }
            });
            return;
          }
          
          observableMpoList.add(newFile.getName());
          System.out.println("added mpoi"); //$NON-NLS-1$
          
          final MPOFile mpo;
          try
          {
            mpo = new MPOFile(newFile);
          }
          catch (IOException e)
          {
            e.printStackTrace();
            return;
          }
          
          final ImageView iView = new ImageView(mpo.getThumbnail());
          
          Platform.runLater(new Runnable()
          {
            @Override
            public void run()
            {
              Tooltip tooltip = new Tooltip(mpo.getFilename());
              Tooltip.install(iView, tooltip);
            }
          });
          
          iView.setPreserveRatio(true);
          iView.fitWidthProperty().bind(imageScrollPane.widthProperty().subtract(75)); // der
          // View
          // für
          // die
          // Preview
          // Pics
          // immer
          // 80
          // weniger
          // breit
          // als
          // der
          // Scrollpane
          
          iView.setUserData(mpo);
          iView.setOnMouseClicked(new EventHandler<MouseEvent>()
          {
            @Override
            public void handle(MouseEvent event)
            {
              ObservableList<Node> children = boxImages.getChildren();
              for (Node node : children)
              {
                node.getStyleClass().remove("selected_thumbnail"); //$NON-NLS-1$
              }
              selectedNode = iView.getParent();
              
              computeScales();
              
              selectedNode.setUserData(iView);
              selectedNode.getStyleClass().add("selected_thumbnail"); //$NON-NLS-1$
              
              if (selectedMPO != null)
                selectedMPO.deleteTempData();
              
              selectedMPO = (MPOFile) iView.getUserData();
              reloadMainImage(MPOFile.scalePreview);
              
            }
          });
          
          Platform.runLater(new Runnable()
          {
            @Override
            public void run()
            {
              ObservableList<Node> children = boxImages.getChildren();
              BorderPane pane = new BorderPane(iView);
              pane.setUserData(iView);
              pane.getStyleClass().add("thumbnail"); //$NON-NLS-1$
              children.add(pane);
              VBox.setMargin(pane, new Insets(0, 25, 0, 25)); // Ausrichten
              // des
              // Images
              // in
              // der
              // Pane
              
              if (children.size() == 1) // Hauptbild laden
              // nachdem das
              // erste Preview
              // Bild geladen
              // ist
              {
                pane.getStyleClass().add("selected_thumbnail"); //$NON-NLS-1$
                selectedMPO = (MPOFile) iView.getUserData();
                selectedNode = iView.getParent();
                selectedNode.requestFocus();
                
                reloadMainImage(MPOFile.scalePreview);
              }
              
            }
          });
        }
      }
    }).start();
  }
  
  private void fillImages(List<File> filesFromDir)
  {
    if (filesFromDir == null || filesFromDir.size() == 0)
      return;
    
    if (filesFromDir.size() == 1 && filesFromDir.get(0).isDirectory())
    {
      File[] listFiles = filesFromDir.get(0).listFiles();
      filesFromDir = Arrays.asList(listFiles);
    }
    
    final List<File> files = filesFromDir;
    
    doesReload = false;
    
    addFiles(files);
  }
  
}
