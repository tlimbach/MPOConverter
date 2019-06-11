package main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class MainImagePane extends StackPane {
	private ZoomCanvas canvas;
	private ImageView mainImageView;
	private MainUi mainUi;

	public MainImagePane(MainUi mainUi, BorderPane parent) {
		this.mainUi = mainUi;
		mainImageView = new ImageView();

		getStyleClass().add("mainimage");
		getStyleClass().add("main");

		mainImageView.setPreserveRatio(true);

		mainImageView.fitWidthProperty().bind(
				parent.widthProperty().subtract(280));

		mainImageView.fitHeightProperty().bind(
				parent.heightProperty().subtract(160));

		mainImageView.fitHeightProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {
						canvas.setHeight(mainImageView.getBoundsInParent()
								.getHeight());
					}
				});

		mainImageView.fitWidthProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {
						canvas.setWidth(mainImageView.getBoundsInParent()
								.getWidth());
					}
				});

		canvas = new ZoomCanvas(mainUi);
		getChildren().addAll(mainImageView, canvas);
	}

	public void setImage(final Image image) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				mainImageView.setImage(image);
				DropShadow dropShadow = new DropShadow();
				dropShadow.setRadius(7.0);
				dropShadow.setOffsetX(6.0);
				dropShadow.setOffsetY(6.0);
				dropShadow.setBlurType(BlurType.GAUSSIAN);
				double gray = 0.4D;
				dropShadow.setColor(Color.color(gray, gray, gray));
				mainImageView.setEffect(dropShadow);

				canvas.setWidth(mainImageView.getBoundsInParent().getWidth());
				canvas.setHeight(mainImageView.getBoundsInParent().getHeight());

			}
		});

	}
}
