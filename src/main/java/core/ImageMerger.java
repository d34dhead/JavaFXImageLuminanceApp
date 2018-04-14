package core;

import org.apache.log4j.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*merges multiple images with different exposures into one luminance matrix
* NOTE: does not support external formula calculation, only default formula with coefficients present in config.properties file*/

public class ImageMerger {
    private ImageProcessor imgProcessor;
    private ImageDataCache cache;
    private PropertiesManager props;

    private static final Logger logger = Logger.getRootLogger();


    public ImageMerger(ImageProcessor imgProcessor, ImageDataCache cache) {
        this.imgProcessor = imgProcessor;
        this.cache = cache;
    }

    public MergedImage MergeImages(List<UnmergedImage> images) {
        long totalStart = System.nanoTime();
        props = new PropertiesManager();
        double coeffA = Double.parseDouble(props.getProperty("coefficientA"));
        double coeffB = Double.parseDouble(props.getProperty("coefficientB"));

        MergedImage merged = new MergedImage(images);
        //initialize remaining images
        initializeImages(images);

        UnmergedImage baseImage = images.get(0);

        /*make a copy both arrays to not affect original object*/
        double[][] enhancedLlabMatrix = Arrays.stream(baseImage.getlLabMatrix())
                .map(double[]::clone)
                .toArray(double[][]::new);
        double[][] mergedLuminanceMatrix = Arrays.stream(baseImage.getLuminanceMatrix())
                .map(double[]::clone)
                .toArray(double[][]::new);

        ArrayList<UnmergedImage> potentialReplacements = new ArrayList<>();
        for (int j = 0; j < enhancedLlabMatrix.length; j++) {
            logger.debug("Processing column no. " + j);
            for (int i = 0; i < enhancedLlabMatrix[0].length; i++) {

                //if value is invalid, look for a valid value in another matrix at the same index and replace with the best fit
                if (!validLValue(enhancedLlabMatrix[j][i])) {
                    long startTime = System.nanoTime();
                    for (int k = 0; k < images.size(); k++) {
                        if(k == images.indexOf(baseImage)) continue;

                        UnmergedImage currentImg = images.get(k);
                        double lLabValue = currentImg.getlLabMatrix()[j][i];
                        if (validLValue(lLabValue)) {
                            potentialReplacements.add(currentImg);
                        }
                    }
                    if (!(potentialReplacements.size() == 0)) {
                        //find image with replacing value closest to Llab = 50
                        UnmergedImage replacingImage = closestTo50(potentialReplacements, j, i);
                        //replace llab value
                        enhancedLlabMatrix[j][i] = replacingImage.getlLabMatrix()[j][i];
                        //replace luminance value
                        mergedLuminanceMatrix[j][i] = imgProcessor.calculatePixelLuminance(replacingImage, j, i, coeffA, coeffB);
                        long totalTime = System.nanoTime() - startTime;
                        logger.debug("Replacing the pixel took " + totalTime / 1000000.0 + " ms");
                    }
                    potentialReplacements.clear();
                }
            }
        }
        merged.setlLabMatrix(enhancedLlabMatrix);
        merged.setLuminanceMatrix(mergedLuminanceMatrix);
        props = null;
        logger.debug("Total processing time: " + (System.nanoTime() - totalStart)/1000000000d + "seconds");
        return merged;
    }

    public List<UnmergedImage> initializeImages(List<UnmergedImage> images){

        for(UnmergedImage img : images){
            if(!img.isInitialized()){
               cache.initializeImageMatrices(img, false);
            }
        }
        return images;
    }

    //find the number closest to 50
    private UnmergedImage closestTo50(List<UnmergedImage> contenders, int j, int i) {
        UnmergedImage closest = contenders.get(0);

        for (int a = 1; a < contenders.size(); a++) {
            double currentValue = contenders.get(a).getlLabMatrix()[j][i];
            if (Math.abs(closest.getlLabMatrix()[j][i] - 50) > Math.abs(currentValue - 50)){
                closest = contenders.get(a);
            }
        }
        return closest;
    }

    private boolean validLValue(double value) {
        return (value > 20) && (value < 80);
    }

    public int countInvalidPixels(double[][] matrix) {
        int count = 0;

        int cols = matrix[0].length;
        int rows = matrix.length;

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if (!validLValue(matrix[j][i])) count++;
            }
        }

        return count;
    }
}
