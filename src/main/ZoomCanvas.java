package main;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ZoomCanvas extends Canvas {
	protected double mouseDownX;
	protected double mouseDownY;
	protected double mouseEndX;
	protected double mouseEndY;
	private MainUi mainUi;

	public ZoomCanvas(MainUi mainUi) {
		super();
		this.mainUi = mainUi;

		setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseDownX = event.getX();
				mouseDownY = event.getY();
			}
		});

		setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (ZoomCanvas.this.mainUi.getSelectedMpoFile().isZoomed())
					return;

				GraphicsContext gc = getGraphicsContext2D();
				gc.clearRect(0, 0, getWidth(), getHeight());
				gc.setStroke(Color.BLUE);
				gc.setLineWidth(2);

				double startX = mouseDownX;
				double startY = mouseDownY;
				double endX = event.getX();
				double endY = event.getY();

				if (endX < startX) {
					double tmp = startX;
					startX = endX;
					endX = tmp;
				}

				if (endY < startY) {
					double tmp = startY;
					startY = endY;
					endY = tmp;
				}

				gc.strokeRect(startX, startY, endX - startX, endY - startY);
			}
		});

		setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				if (ZoomCanvas.this.mainUi.getSelectedMpoFile().isZoomed())
					return;

				mouseEndX = event.getX();
				mouseEndY = event.getY();

				double imgWidth = getWidth();
				double imgHeight = getHeight();

				float startX = (float) (mouseDownX * 100 / imgWidth);
				float startY = (float) (mouseDownY * 100 / imgHeight);
				float endX = (float) (mouseEndX * 100 / imgWidth);
				float endY = (float) (mouseEndY * 100 / imgHeight);

				GraphicsContext gc = getGraphicsContext2D();
				gc.clearRect(0, 0, getWidth(), getHeight());

				if (endX < startX) {
					float tmp = startX;
					startX = endX;
					endX = tmp;
				}

				if (endY < startY) {
					float tmp = startY;
					startY = endY;
					endY = tmp;
				}

				if (endX - startX > 5 && endY - startY > 5) {
					ZoomCanvas.this.mainUi.getSelectedMpoFile().setRange(
							imgWidth, imgHeight, startX, startY, endX, endY);
					ZoomCanvas.this.mainUi
							.reloadMainImage(MPOFile.scalePreview);
					ZoomCanvas.this.mainUi.btnResetZoom.setDisable(false);
				} else {
					UIHelper.showMessage(
							Messages.getString("ZoomCanvas.0"), //$NON-NLS-1$
							false);
				}

			}
		});
	}
}
