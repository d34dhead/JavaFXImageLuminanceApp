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

    private static final Logger logger = Logger.getRootLogger();


    public ImageMerger(ImageProcessor imgProcessor, ImageDataCache cache) {
        this.imgProcessor = imgProcessor;
        this.cache = cache;
    }

    public MergedImage MergeImages(List<UnmergedImage> images) {

        MergedImage merged = new MergedImage(images);
        //vypocti zbyle matice z nahranych fotografii
        initializeImages(images);
        //zvolme referencni fotografii
        UnmergedImage baseImage = images.get(0);

        //vytvor kopii obou matic abys nezasahoval do puvodnich
        double[][] mergedLlabMatrix = Arrays.stream(baseImage.getlLabMatrix())
                .map(double[]::clone)
                .toArray(double[][]::new);
        double[][] mergedLuminanceMatrix = Arrays.stream(baseImage.getLuminanceMatrix())
                .map(double[]::clone)
                .toArray(double[][]::new);

        //zavedme list pro ukladani potencialnich nahrad
        ArrayList<UnmergedImage> potentialReplacements = new ArrayList<>();

        for (int j = 0; j < mergedLlabMatrix.length; j++) {
            for (int i = 0; i < mergedLlabMatrix[0].length; i++) {
                //iteruj mezi jednotlivymi fotografiemi a hledej vhodnejsi nahrady
                for (UnmergedImage img : images) {
                    double lLabValue = img.getlLabMatrix()[j][i];
                    if (validLValue(lLabValue)) {
                        potentialReplacements.add(img);
                    }
                }
                //vyber z potencialnich nahrad tu nejblize LLab = 50
                //pote uloz do referencni
                if (!(potentialReplacements.size() == 0)) {
                    UnmergedImage replacingImage =
                            closestTo50(potentialReplacements, j, i);
                    if(replacingImage != baseImage) {
                        //replace llab value
                        mergedLlabMatrix[j][i] =
                                replacingImage.getlLabMatrix()[j][i];
                        //replace luminance value
                        mergedLuminanceMatrix[j][i] =
                                replacingImage.getLuminanceMatrix()[j][i];
                    }
                }
                potentialReplacements.clear();
            }
        }
        merged.setlLabMatrix(mergedLlabMatrix);
        merged.setLuminanceMatrix(mergedLuminanceMatrix);
        return merged;
    }

    private List<UnmergedImage> initializeImages(List<UnmergedImage> images) {

        for (UnmergedImage img : images) {
            if (!img.isInitialized()) {
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
            if (Math.abs(closest.getlLabMatrix()[j][i] - 50)
                    > Math.abs(currentValue - 50)) {
                closest = contenders.get(a);
            }
        }
        return closest;
    }

    private boolean validLValue(double value) {
        return (value > 20) && (value < 80);
    }

}
