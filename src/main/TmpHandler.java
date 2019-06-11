package main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;

public class TmpHandler {

	private static TmpHandler handler;
	String DIR;

	public static TmpHandler getXInstance() {

		if (handler == null)
			handler = new TmpHandler();

		return handler;
	}

	public void createTmp() {
		try {
			DIR = new File(".").getCanonicalPath() + "/tmp/";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new File(DIR).mkdirs();
	}

	private TmpHandler() {
		try {
			DIR = new File(".").getCanonicalPath() + "/tmp/";
			new File(DIR).mkdirs();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("dir = " + DIR);
	}

	public Image get(String key) {
		try {

			key = DIR + "/" + key;
			File input = new File(key);

			if (!input.exists())
				return null;

			BufferedImage bImage = ImageIO.read(input);
			return SwingFXUtils.toFXImage(bImage, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	synchronized public void put(String key, Image image) {
		String DIR;
		try {
			DIR = new File(".").getCanonicalPath() + "/tmp/";
			key = DIR + "/" + key;
			writeImageToDisk(key, image);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void clear() {
		File fileDir = new File(DIR);

		if (!fileDir.exists())
			return;

		for (File file : fileDir.listFiles()) {
			file.delete();
		}

		fileDir.delete();
	}

	public void writeImageToDisk(String filename, Image imageToSave) {
		if (!new File(DIR).exists())
			createTmp();

		try {
			BufferedImage image = SwingFXUtils.fromFXImage(imageToSave, null);
			BufferedImage imageRGB = new BufferedImage(image.getWidth(),
					image.getHeight(), BufferedImage.OPAQUE);
			Graphics2D graphics = imageRGB.createGraphics();
			graphics.drawImage(image, 0, 0, null);
			File output = new File(filename + ".tmp");
			ImageIO.write(imageRGB, "jpg", output);
			graphics.dispose();
			output.renameTo(new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isCached(String key) {
		try {
			DIR = new File(".").getCanonicalPath() + "/tmp/";
			key = DIR + "/" + key;
			boolean fileExists = new File(key).exists();

			return fileExists;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void clearMpo(MPOFile mpoFile) {
		File fileDir = new File(DIR);

		if (!fileDir.exists())
			return;

		for (File file : fileDir.listFiles()) {
			if (file.getName().contains(mpoFile.getFilename()))
				file.delete();
		}

	}

}
