package core;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorMapper {
    private static ImageDataCache cache = ImageDataCache.getInstance();

    // using default colors and threshholds
    public static ReconstructedImage reconstructImage(MyImage image) {
        if (!image.isInitialized()) {
            cache.initializeImageMatrices(image, false);
        }

        Color[] colors = ReconstructedImage.getDefaultColors();

        double[][] llabMatrix = image.getlLabMatrix();
        double[][] luminanceMatrix = image.getLuminanceMatrix();

        double[] intervalThreshholds = calculateThreshholds(image, colors.length);

        int height = image.getlLabMatrix().length;
        int width = image.getlLabMatrix()[0].length;

        //construct an empty img with given dimensions
        ReconstructedImage reconstructedImg = new ReconstructedImage(width, height, BufferedImage.TYPE_INT_RGB);

        //fill image by adding pixels one by one
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                //get L value
                double luminance = luminanceMatrix[j][i];
                double llab = llabMatrix[j][i];
                Color color;
                //set pixel color based on L value
                if (llab >= 80) {
                    color = Color.WHITE;
                    reconstructedImg.setRGB(i, j, color.getRGB());
                } else if (llab <= 20) {
                    color = Color.BLACK;
                    reconstructedImg.setRGB(i, j, color.getRGB());
                } else {
                    for (int k = 0; k < intervalThreshholds.length; k++) {
                        if (luminance >= intervalThreshholds[k]) {
                            color = colors[k];
                            reconstructedImg.setRGB(i, j, color.getRGB());
                            break;
                        }
                    }
                }
            }
        }
        reconstructedImg.setLuminanceThreshholds(intervalThreshholds);
        return reconstructedImg;
    }

    // using default colors and threshholds
    public static ReconstructedImage reconstructImage(MyImage image, ReconstructedImage reconstructedImg) {
        double[][] luminanceMatrix = image.getLuminanceMatrix();
        Color[] colors = reconstructedImg.getColors();
        double[] luminanceThreshholds = reconstructedImg.getLuminanceThreshholds();

        int height = luminanceMatrix.length;
        int width = luminanceMatrix[0].length;

        //fill image by adding pixels one by one
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                //get L value
                double luminance = luminanceMatrix[j][i];
                Color color;
                if(luminance != 0 && luminance != 9999) {
                    //set pixel color based on L value
                    for (int k = 0; k < luminanceThreshholds.length; k++) {
                        if (luminance >= luminanceThreshholds[k]) {
                            color = colors[k];
                            reconstructedImg.setRGB(i, j, color.getRGB());
                            break;
                        }
                    }
                }
            }
        }
        return reconstructedImg;
    }

    private static double[] calculateThreshholds(MyImage image, int noOfIntervals) {
        double[] threshholds = new double[noOfIntervals];

        double maxL = image.getMaxLuminance();
        double minL = image.getMinLuminance();

        double intervalSize = (maxL - minL)/noOfIntervals;

        for (int i = 0; i < threshholds.length; i++) {
                if (i == 0) {
                threshholds[i] = maxL - intervalSize;
                continue;
            }

            if (i == threshholds.length - 1) {
                threshholds[i] = minL;
                break;
            }
            threshholds[i] = maxL - (i + 1) * intervalSize;
        }

        return threshholds;
    }


}

