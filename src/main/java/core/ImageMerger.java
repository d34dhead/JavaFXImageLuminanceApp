package core;

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

    public ImageMerger(ImageProcessor imgProcessor, ImageDataCache cache) {
        this.imgProcessor = imgProcessor;
        this.cache = cache;
    }

    public MergedImage MergeImages(List<UnmergedImage> images) {
        props = new PropertiesManager();
        double coeffA = Double.parseDouble(props.getProperty("coefficientA"));
        double coeffB = Double.parseDouble(props.getProperty("coefficientB"));

        MergedImage merged = new MergedImage(images);

        UnmergedImage initializedImage = findFirstInitializedImage(images);

        /*did not find any initialized img
         * take the first image in the List and initialize it*/
        if(initializedImage == null) {
            initializedImage = images.get(0);
            cache.initializeImageMatrices(initializedImage, false);
        }

        /*make a copy both arrays to not affect original object*/
        double[][] enhancedLlabMatrix = Arrays.stream(initializedImage.getlLabMatrix())
                .map(double[]::clone)
                .toArray(double[][]::new);
        double[][] mergedLuminanceMatrix = Arrays.stream(initializedImage.getLuminanceMatrix())
                .map(double[]::clone)
                .toArray(double[][]::new);

        ArrayList<UnmergedImage> potentialReplacements = new ArrayList<>();

        for (int i = 0; i < enhancedLlabMatrix[0].length; i++) {

            for (int j = 0; j < enhancedLlabMatrix.length; j++) {

                //if value is invalid, look for a valid value in another matrix at the same index and replace with the best fit
                if (!validLValue(enhancedLlabMatrix[j][i])) {
                    for (int k = 0; k < images.size(); k++) {
                        if(k == images.indexOf(initializedImage)) continue;

                        double lLabValue = images.get(k).getlLabMatrix()[j][i];
                        if (validLValue(lLabValue)) {
                            potentialReplacements.add(images.get(k));
                        }
                    }
                    if (potentialReplacements.isEmpty()) continue;
                    //find image with replacing value closest to Llab = 50
                    UnmergedImage replacingImage = closestTo50(potentialReplacements, j, i);
                    //replace llab value
                    enhancedLlabMatrix[j][i] = replacingImage.getlLabMatrix()[j][i];
                    //replace luminance value
                    mergedLuminanceMatrix[j][i] = imgProcessor.calculatePixelLuminance(replacingImage, j, i, coeffA, coeffB);
                }
            }
        }
        merged.setlLabMatrix(enhancedLlabMatrix);
        merged.setLuminanceMatrix(mergedLuminanceMatrix);
        props = null;
        return merged;
    }

    public UnmergedImage findFirstInitializedImage(List<UnmergedImage> images){

        for(UnmergedImage img : images){
            if(img.isInitialized()){
               return img;
            }
        }
        return null;
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

    /*//returns an Unmerged image with the least amount of invalid pixels (Llab <=20 or >=80)
    public UnmergedImage findImgWithLeastInvalidPixels(List<UnmergedImage> images) {
        UnmergedImage leastInvalidPixelsImage = images.get(0);

        int cols = leastInvalidPixelsImage.getImg().getWidth();
        int rows = leastInvalidPixelsImage.getImg().getHeight();

        //initialize as maximum value
        int lowestInvalidPixelCount = rows * cols;

        for (int i = 0; i < images.size(); i++) {
            UnmergedImage currentImg = images.get(i);
            double[][] currentMatrix = currentImg.getlLabMatrix();
            int invalidCount = this.countInvalidPixels(currentMatrix);

            if (invalidCount < lowestInvalidPixelCount) {
                lowestInvalidPixelCount = invalidCount;
                leastInvalidPixelsImage = currentImg;
            }
        }
        return leastInvalidPixelsImage;
    }*/

    private boolean validLValue(double value) {
        return !(value <= 20) && !(value >= 80);
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
