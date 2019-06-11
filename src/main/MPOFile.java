package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.monte.media.exif.EXIFReader;
import org.monte.media.math.Rational;
import org.monte.media.tiff.TIFFField;
import org.monte.media.tiff.TIFFTag;

public class MPOFile {

    private static final double CONTRAST_CORRECTION_VALUE = 0.00035;

    private File mpoFile;

    private Image imageThumb = null;

    double parallaxX = 0.0;
    double parallaxY = 0.0;

    double originalParallaxX;

    private boolean userAutoContrast = ColorAdjustmentNode.defaultAutoContrast;
    private int userSaturation = 0;
    private int userBrightness = 0;

    private int mpoOriginalWidth = -1;

    public static int scaleMonitor = 2;
    public static int scalePreview = 3;
    public static int scaleHiRes = 1;
    public static int scaleParallaxeShift = 4;

    Map<String, BufferedImage> mapBImages = new HashMap<String, BufferedImage>();

    private float startX, startY, endX, endY;

    public MPOFile(File mpoFile) throws IOException {
        this.mpoFile = mpoFile;
        resetZoom();
        readParallax();
    }

    public int getMpoOriginalWidth() {
        return mpoOriginalWidth;
    }

    public Image getThumbnail() {
        int thumbailScale = 7;

        BufferedImage imgFile1;
        if (imageThumb == null) {

            ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceSubsampling(thumbailScale, thumbailScale, 0, 0);
            try {
                ImageInputStream iis = ImageIO.createImageInputStream(mpoFile);

                reader.setInput(iis, true);
                imgFile1 = reader.read(0, param);
                iis.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            int imgWidth = imgFile1.getWidth();
            int ingHeight = imgFile1.getHeight();

            mpoOriginalWidth = imgWidth * thumbailScale;

            int scaledWidth = 300;
            int scaledHeight = ingHeight * scaledWidth / imgWidth;

            java.awt.Image scaledInstance = imgFile1.getScaledInstance(scaledWidth, scaledHeight, 0);

            imageThumb = SwingFXUtils.toFXImage(UIHelper.toBufferedImage(scaledInstance), null);
        }

        return imageThumb;
    }

    public Image get2DImage(int scale) throws FileWasDeletedException {

        if (!mpoFile.exists()) {
            throw new FileWasDeletedException();
        }

        boolean _autoContrast = getUserAutoContrast();
        int _saturation = getUserSaturationDiff();
        int _brightness = getUserBrightnessDiff();

        BufferedImage img2DFile1 = mapBImages.get("1" + scale);

        if (img2DFile1 == null) {
            // Image Dateien lesen
            ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
            ImageReadParam param = reader.getDefaultReadParam();

            param.setSourceSubsampling(scale, scale, 0, 0);

            try {
                ImageInputStream iis = ImageIO.createImageInputStream(mpoFile);

                reader.setInput(iis, true);
                img2DFile1 = reader.read(0, param);
                iis.close();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            mapBImages.put("1" + scale, img2DFile1);
        }

        int imgWidth = img2DFile1.getWidth();
        int imgHeight = img2DFile1.getHeight();

        if (isZoomed()) {

            try {
                int newWidth = (int) (imgWidth / 100D * (endX - startX));
                int newHeight = (int) (imgHeight / 100D * (endY - startY));

                int newX0 = (int) (imgWidth / 100D * startX);
                int newY0 = (int) (imgHeight / 100D * startY);

                BufferedImage imgTmp = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < newWidth; x++) {
                    for (int y = 0; y < newHeight; y++) {
                        imgTmp.setRGB(x, y, img2DFile1.getRGB(x + newX0, y + newY0));
                    }
                }

                img2DFile1 = imgTmp;
                imgWidth = img2DFile1.getWidth();
                imgHeight = img2DFile1.getHeight();
            } catch (Exception e) {
                resetZoom();
                e.printStackTrace();
            }
        }

        Dimension contrastDim = null;
        if (_autoContrast) {
            contrastDim = getMinMax(img2DFile1);
        }

        BufferedImage imgConverted = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < imgWidth; x++) {
            for (int y = 0; y < imgHeight; y++) {
                int rgbOne = img2DFile1.getRGB(x, y);
                Color col1 = new Color(rgbOne);
                col1 = adjustSaturationBrightnessContrast(col1, _saturation, _brightness, contrastDim);
                rgbOne = col1.getRGB();
                imgConverted.setRGB(x, y, rgbOne);
            }
        }

        return SwingFXUtils.toFXImage(imgConverted, null);
    }

    public Image get3DImage(int scale, boolean isSave) throws FileWasDeletedException {

        if (!mpoFile.exists()) {
            throw new FileWasDeletedException();
        }

        boolean _enhancedContrast = userAutoContrast;
        int _saturation = getUserSaturationDiff();
        int _brightness = getUserBrightnessDiff();

        BufferedImage img3DFile1 = mapBImages.get("1" + scale);
        BufferedImage img3DFile2 = mapBImages.get("2" + scale);

        if (img3DFile1 == null || img3DFile2 == null) {
            ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
            ImageReadParam param = reader.getDefaultReadParam();

            param.setSourceSubsampling(scale, scale, 0, 0);
            try {
                ImageInputStream iis = ImageIO.createImageInputStream(mpoFile);

                reader.setInput(iis, true);
                img3DFile1 = reader.read(0, param);
                while (iis.read() == 0)
          ;
                iis.seek(iis.getStreamPosition() - 1);
                reader.reset();
                reader.setInput(iis, true);
                img3DFile2 = reader.read(0, param);

                iis.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            mapBImages.put("1" + scale, img3DFile1);
            mapBImages.put("2" + scale, img3DFile2);
        }

        BufferedImage imgTmp;
        BufferedImage imgTmp2;

        if (isZoomed()) {

            try {
                int imgWidth = (img3DFile1.getWidth());
                int imgHeight = img3DFile1.getHeight();
                int newWidth = (int) (imgWidth / 100D * (endX - startX));
                int newHeight = (int) (imgHeight / 100D * (endY - startY));

                int newX0 = (int) (imgWidth / 100D * startX);
                int newY0 = (int) (imgHeight / 100D * startY);

                imgTmp = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < newWidth; x++) {
                    for (int y = 0; y < newHeight; y++) {
                        imgTmp.setRGB(x, y, img3DFile1.getRGB(x + newX0, y + newY0));
                    }
                }

                img3DFile1 = imgTmp;

                imgWidth = img3DFile2.getWidth();
                imgHeight = img3DFile2.getHeight();
                newWidth = (int) (imgWidth / 100D * (endX - startX));
                newHeight = (int) (imgHeight / 100D * (endY - startY));

                newX0 = (int) (imgWidth / 100D * startX);
                newY0 = (int) (imgHeight / 100D * startY);

                imgTmp2 = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < newWidth; x++) {
                    for (int y = 0; y < newHeight; y++) {
                        imgTmp2.setRGB(x, y, img3DFile2.getRGB(x + newX0, y + newY0));
                    }
                }

                img3DFile2 = imgTmp2;
            } catch (Exception e) {
                e.printStackTrace();
                resetZoom();
            }
        }

        Dimension contrastDim = null;
        if (_enhancedContrast) {
            contrastDim = getMinMax(img3DFile1);
        }

        int transpositionX = (int) ((img3DFile1.getWidth()) * (parallaxX / 100D));
        int transpositionY = (int) ((img3DFile1.getHeight()) * (parallaxY / 100D));

        int width = img3DFile1.getWidth() - Math.abs(transpositionX);
        int height = img3DFile1.getHeight() - Math.abs(transpositionY);

       

        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("size to smal!!");
        }

        BufferedImage imgConverted = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgbOne = 0, rgbTwo = 0;

                if (transpositionX > 0 && transpositionY >= 0) {
                    rgbOne = img3DFile1.getRGB(x + transpositionX, y + transpositionY);
                    rgbTwo = img3DFile2.getRGB(x, y);
                } else if (transpositionX > 0 && transpositionY < 0) {
                    rgbOne = img3DFile1.getRGB(x + transpositionX, y);
                    rgbTwo = img3DFile2.getRGB(x, y - transpositionY);
                } else if (transpositionX < 0 && transpositionY <= 0) {
                    rgbOne = img3DFile1.getRGB(x, y);
                    rgbTwo = img3DFile2.getRGB(x - transpositionX, y - transpositionY);
                } else if (transpositionX < 0 && transpositionY > 0) {
                    rgbOne = img3DFile1.getRGB(x, y + transpositionY);
                    rgbTwo = img3DFile2.getRGB(x - transpositionX, y);
                } else {
                    rgbOne = img3DFile1.getRGB(x, y);
                    rgbTwo = img3DFile2.getRGB(x, y);
                }

                Color col1 = new Color(rgbOne);
                Color col2 = new Color(rgbTwo);
                col1 = adjustSaturationBrightnessContrast(col1, _saturation, _brightness, contrastDim);
                col2 = adjustSaturationBrightnessContrast(col2, _saturation, _brightness, contrastDim);

                rgbOne = col1.getRGB();
                rgbTwo = col2.getRGB();

                int rgbResult = (rgbTwo & 0xFF00FFFF) | (rgbOne & 0xFFFF0000);

                imgConverted.setRGB(x, y, rgbResult);

            }
        }

        
        if (isSave) {
              imgConverted = joinBufferedImage(img3DFile1, img3DFile2, transpositionX, _saturation, _brightness, contrastDim);
        }

        return SwingFXUtils.toFXImage(imgConverted, null);
    }

    private int adjustRGB(int rgb, int saturation, int brightness, Dimension contrast) {
        Color col = new Color(rgb);
        col = adjustSaturationBrightnessContrast(col, saturation, brightness, contrast);

        return col.getRGB();
    }

    public BufferedImage joinBufferedImage(BufferedImage img1,
            BufferedImage img2, int xOffset, int saturation, int brightness, Dimension contrastDim) {

        System.out.println("incoming tranposition :" + xOffset);

        xOffset = -xOffset;
        int width = img1.getWidth() + img2.getWidth() - (2 * Math.abs(xOffset));
        int height = img1.getHeight();

        System.out.println("img1 " + img1.getWidth());
        System.out.println("img2 " + img2.getWidth());
        System.out.println("xOffset " + xOffset);
        System.out.println("newImageWifth  " + width);

        BufferedImage imgConverted = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int x = 0;
        int currImg2x = 0;
        try {
            int width1 = img1.getWidth() - Math.abs(xOffset);
            int width2 = img2.getWidth() - Math.abs(xOffset);

            if (xOffset >= 0) {

                for (x = 0; x < width1 - 1; x++) {
                    for (int y = 0; y < height; y++) {

                        int rgb = img1.getRGB(x, y);
                        rgb = adjustRGB(rgb, saturation, brightness, contrastDim);
                        imgConverted.setRGB(x, y, rgb);

                    }

                }

                for (int x2 = 0; x2 < width2 - 1; x2++) {
                    x++;
                    for (int y = 0; y < height; y++) {

                        int rgb = img2.getRGB(x2 + xOffset, y);
                        rgb = adjustRGB(rgb, saturation, brightness, contrastDim);
                        imgConverted.setRGB(x, y, rgb);

                    }
                }
            } else {

                for (x = 0; x < width1 - 2; x++) {
                    for (int y = 0; y < height; y++) {

                        int rgb = img1.getRGB(x - xOffset, y);
                        rgb = adjustRGB(rgb, saturation, brightness, contrastDim);
                        imgConverted.setRGB(x, y, rgb);

                    }

                }

                for (int x2 = 0; x2 < width2 - 1; x2++) {
                    x++;
                    for (int y = 0; y < height; y++) {

                        int rgb = img2.getRGB(x2, y);
                        rgb = adjustRGB(rgb, saturation, brightness, contrastDim);
                        imgConverted.setRGB(x, y, rgb);

                    }
                }
            }

        } catch (Exception e) {
            System.out.println("x currently  " + x);
            System.out.println("img1getWidth  " + img1.getWidth());
            System.out.println("img2x currenty " + currImg2x);
            System.out.println("xOffset " + xOffset);

            throw e;
        }
        return imgConverted;
    }

    public void deleteTempData() {
        mapBImages.clear();
    }

    private Color adjustSaturationBrightnessContrast(Color color,
            double saturation,
            double brightness,
            Dimension contrastDim) {

        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);

        if (contrastDim != null) {
            double minContrast = 0;

            if (contrastDim.getWidth() > 0) {
                minContrast = contrastDim.getWidth() / 100D;
            }

            double maxContrast = 0;

            if (contrastDim.getHeight() > 0) {
                maxContrast = contrastDim.getHeight() / 100D;
            }

            if (contrastDim != null) {

                hsv[2] = (float) (hsv[2] * (1D / maxContrast));
                hsv[2] = (float) (1 - ((1 - hsv[2]) / (1 - minContrast)));

            }
        }

        hsv[1] += hsv[1] * (float) (saturation * 0.01D);
        hsv[2] += hsv[2] * (float) (brightness * 0.01D);

        if (hsv[1] > 1) {
            hsv[1] = 1;
        } else if (hsv[1] < 0) {
            hsv[1] = 0;
        }
        if (hsv[2] > 1) {
            hsv[2] = 1;
        } else if (hsv[2] < 0) {
            hsv[2] = 0;
        }

        return new Color(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));

    }

    private Dimension getMinMax(BufferedImage inputFile) {

        int[] bright = new int[101];

        int width = inputFile.getWidth();
        int height = inputFile.getHeight();

        for (int x = 0; x < width; x += 10) {
            for (int y = 0; y < height; y += 10) {
                int rgb = inputFile.getRGB(x, y);
                Color color = new Color(rgb);
                float[] hsv = new float[3];
                Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);

                bright[(int) (hsv[2] * 100D)]++;

            }
        }

        int totalPixels = (width * height) / 100;
        int minAmount = (int) (totalPixels * CONTRAST_CORRECTION_VALUE);

        int min = 0, max = 100;
        for (int t = (max - 1); t > 0; t--) {
            if (bright[t] > minAmount) {
                break;
            }
            max--;
        }

        for (int t = min; t < max; t++) {
            if (bright[t] > minAmount) {
                break;
            }
            min++;
        }

        return new Dimension(min, max);
    }

    private void readParallax() throws IOException {
        EXIFReader exifReader = new EXIFReader(mpoFile);
        exifReader.read();
        HashMap<TIFFTag, TIFFField> metaDataMap = exifReader.getMetaDataMap();
        Set<TIFFTag> set = metaDataMap.keySet();
        TIFFField tiffField = null;
        for (TIFFTag tag : set) {

            if (tag.getName().equals("ParallaxXShift")) {
                tiffField = metaDataMap.get(tag);
                Rational rat = (Rational) tiffField.getData();
                long denominator = rat.getDenominator();
                long numerator = rat.getNumerator();

                try {
                    parallaxX = (double) numerator / (double) denominator;
                    originalParallaxX = parallaxX;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (tag.getName().equals("ParallaxYShift")) {
                tiffField = metaDataMap.get(tag);
                Rational rat = (Rational) tiffField.getData();
                long denominator = rat.getDenominator();
                long numerator = rat.getNumerator();

                try {
                    parallaxY = (double) numerator / (double) denominator;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void writeImageToDisk(boolean _3D, File output) throws FileWasDeletedException {
        Image imageToSave = null;
        imageToSave = _3D ? get3DImage(scaleHiRes, true) : get2DImage(MPOFile.scaleHiRes);

        try {
            BufferedImage image = SwingFXUtils.fromFXImage(imageToSave, null);
            BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE);
            final Graphics2D graphics = imageRGB.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            ImageIO.write(imageRGB, "jpg", output);
            graphics.dispose();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getParallaxX() {
        return parallaxX;
    }

    public double getParallaxY() {
        return parallaxY;
    }

    public void setParallax(double newParallaxX, double newParallaxY) {
        parallaxX = newParallaxX;
        parallaxY = newParallaxY;
    }

    public void resetParallax() {
        parallaxX = originalParallaxX;
    }

    public String getFilename() {
        return mpoFile.getName();
    }

    public void delete() {
        mpoFile.deleteOnExit();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mpoFile.delete();
            }
        }, 5000);
    }

    public void setEnhancedContrast(boolean enhancedContrast) {
        this.userAutoContrast = enhancedContrast;
    }

    public boolean getUserAutoContrast() {
        return userAutoContrast;
    }

    public void setUserSaturationDiff(int diff) {
        this.userSaturation = diff;
    }

    public void setUserBrithness(int diff) {
        this.userBrightness = diff;
    }

    public int getUserSaturationDiff() {
        return userSaturation;
    }

    public int getUserBrightnessDiff() {
        return userBrightness;
    }

    public void setRange(double imgWidth, double imgHeight, float startX, float startY, float endX, float endY) {
        double paralaxPrct = 100D * (parallaxX / imgWidth);

        if (paralaxPrct < 0) {
            startX = (float) (startX + paralaxPrct);
        }

        if (paralaxPrct > 0) {
            endX = (float) (endX + paralaxPrct);
        }

        this.startX = correctBounds(startX);
        this.startY = correctBounds(startY);
        this.endX = correctBounds(endX);
        this.endY = correctBounds(endY);

    }

    private float correctBounds(float zeroToOnehundret) {
        if (zeroToOnehundret < 0) {
            zeroToOnehundret = 0;
        }

        if (zeroToOnehundret > 100) {
            zeroToOnehundret = 100;
        }

        return zeroToOnehundret;
    }

    public void resetZoom() {
        startX = -1;
        startY = -1;
        endX = -1;
        endY = -1;

        // deleteTempData();
    }

    public boolean isZoomed() {
        return startX > -1;
    }

    public File getOutputFile(boolean _3D) {
        return new File(mpoFile.getParentFile()
                + "/"
                + mpoFile.getName().substring(0, mpoFile.getName().length() - 4)
                + (_3D ? "_LRF" : "")
                + ".jpg");
    }
}
