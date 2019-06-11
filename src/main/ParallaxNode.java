package main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ParallaxNode extends StandardHBox implements
		EventHandler<ActionEvent> {

	private ShadowButton  btnLeft5, btnLeft1, btnRight5,
			btnRight1, btnReset;

	private MainUi mainUi;

	public ParallaxNode(MainUi mainUi) {
		getStyleClass().add("mainimage"); //$NON-NLS-1$
		this.mainUi = mainUi;
		btnLeft5 = makeButton(Messages.getString("ParallaxNode.3"), "Arrowhead-Left-02-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
		btnLeft1 = makeButton(Messages.getString("ParallaxNode.5"), "Arrowhead-Left-01-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
		btnRight5 = makeButton(Messages.getString("ParallaxNode.9"), "Arrowhead-Right-02-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
		btnRight1 = makeButton(Messages.getString("ParallaxNode.11"), "Arrowhead-Right-01-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
		btnReset = makeButton(Messages.getString("ParallaxNode.13"), "Command-Reset-256.png"); //$NON-NLS-1$ //$NON-NLS-2$

		getChildren().addAll(btnLeft5, btnLeft1, btnRight1, btnRight5
				, btnReset);
	}

	private ShadowButton makeButton(String text, String imageName) {
		return UIHelper.makeButton(this, text, imageName);
	}

	@Override
	public void handle(ActionEvent event) {

		MPOFile mpoFile = mainUi.getSelectedMpoFile();
		double diffParallaxX = 0;

		if (event.getSource() == btnLeft5) {
			diffParallaxX -= 0.5;
		}
		if (event.getSource() == btnLeft1) {
			diffParallaxX -= 0.2;
		}

		if (event.getSource() == btnRight5) {
			diffParallaxX += 0.5;
		}
		if (event.getSource() == btnRight1) {
			diffParallaxX += 0.2;
		}

		if (event.getSource() == btnReset) {
			mpoFile.resetParallax();
			diffParallaxX = 0;
		}

		mpoFile.setParallax(mpoFile.getParallaxX() + diffParallaxX,
				mpoFile.getParallaxY());
		mainUi.reloadMainImage(MPOFile.scaleParallaxeShift);
	}

}
