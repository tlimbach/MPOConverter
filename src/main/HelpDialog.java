package main;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class HelpDialog {
	private Stage dialog;
	private GridPane pane;

	public HelpDialog() {
		dialog = new Stage();
		dialog.setTitle("MPO Converter"); //$NON-NLS-1$
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initStyle(StageStyle.UTILITY);
		pane = new GridPane();

		setContent();

		Scene scene = new Scene(new ScrollPane(pane));

		dialog.setResizable(false);
		dialog.setWidth(1050);
		dialog.setHeight(700);

		dialog.setMinHeight(dialog.getHeight());
		dialog.setMinWidth(dialog.getWidth());
		dialog.setScene(scene);
		dialog.showAndWait();
	}

	private void setContent() {

		ImageView image = makeUnscaledImageView("logo.png"); //$NON-NLS-1$
		setAlignment(image);
		Label lblHeaderline = new Label(Messages.getString("HelpDialog.2")); //$NON-NLS-1$
		setAlignment(lblHeaderline);
		GridPane.setConstraints(lblHeaderline, 1, 2, 2, 1);
		GridPane.setConstraints(image, 1, 1, 2, 1);

		addImageAndText("opensaveall.jpg", //$NON-NLS-1$
				Messages.getString("HelpDialog.4")); //$NON-NLS-1$

		addImageAndText("contrastetc.jpg", //$NON-NLS-1$
				Messages.getString("HelpDialog.6")); //$NON-NLS-1$

		addImageAndText("parallaxe.jpg", //$NON-NLS-1$
				Messages.getString("HelpDialog.8")); //$NON-NLS-1$

		addImageAndText("savedeleteunzoom.jpg", //$NON-NLS-1$
				Messages.getString("HelpDialog.10")); //$NON-NLS-1$

		addImageAndText(null, //$NON-NLS-1$
				Messages.getString("HelpDialog.0")); //$NON-NLS-1$

		pane.getChildren().addAll(lblHeaderline, image);
	}

	int firstContentRow = 3;

	private void addImageAndText(String imageName, String text) {
		firstContentRow++;

		if (imageName != null) {
			ImageView view = makeImageView(imageName);
			GridPane.setConstraints(view, 1, firstContentRow, 1, 1);
			GridPane.setHalignment(view, HPos.RIGHT);
			GridPane.setMargin(view, new Insets(5, 20, 30, 20));
			pane.getChildren().add(view);
		}
		Text lbl = new Text(text);
		GridPane.setConstraints(lbl, 2, firstContentRow, 1, 1);
		lbl.setTextAlignment(TextAlignment.LEFT);
		lbl.setWrappingWidth(550);
		setAlignment(lbl);

		pane.getChildren().add(lbl);

	}

	private void setAlignment(Node node) {
		GridPane.setFillWidth(node, false);
		GridPane.setFillHeight(node, false);

		GridPane.setValignment(node, VPos.CENTER);
		GridPane.setHalignment(node, HPos.CENTER);
		GridPane.setMargin(node, new Insets(5, 20, 30, 20));
	}

	private ImageView makeImageView(String imageName) {
		return new ImageView(new Image(
				UIHelper.class.getResourceAsStream("images/help/" + imageName))); //$NON-NLS-1$
	}

	private ImageView makeScaledImageView(String imageName) {
		int imageSize = 50;
		return new ImageView(UIHelper.getScaled(
				new Image(UIHelper.class
						.getResourceAsStream("images/help/" + imageName)), //$NON-NLS-1$
				imageSize, imageSize));
	}

	private ImageView makeUnscaledImageView(String imageName) {
		return new ImageView(new Image(
				UIHelper.class.getResourceAsStream("images/" + imageName))); //$NON-NLS-1$
	}
}
