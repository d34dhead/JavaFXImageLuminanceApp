package core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

//1. store multiple images in an array
//2. make a Llab matrix of each image
//3. find image with the least values <=20 && >=80 (invalid pixel)
//4. for each invalid pixel, look into other Llab matrixes, if they have a valid number on the same position
//5. replace invalid pixel with a valid pixel from the other image

public class ImageMerger {
    private ImageProcessor imgProcessor;

    public ImageMerger(ImageProcessor imgProcessor) {
        this.imgProcessor = imgProcessor;
    }

    public double[][] MergeImages(BufferedImage[] images) {
        ArrayList<double[][]> llabMatrices = new ArrayList<>();
        for (BufferedImage img : images) {
            llabMatrices.add(imgProcessor.constructLlabMatrix(img));
        }
        return this.MergeLlabMatrices(0, llabMatrices);
    }


    //from given list of Llab matrioes, take the matrix with baseIndex and for each invalid Llab value, look for a valid value on the same index of a
    // different matrix, then replace the invalid value with the valid one
    public double[][] MergeLlabMatrices(int baseIndex, ArrayList<double[][]> llabMatrices) {
        double[][] enhanced = llabMatrices.get(baseIndex);

        for(int i = 0; i < enhanced[0].length; i++){
            for(int j = 0; j < enhanced.length; j++){
                //if value is invalid, look for a valid value in another matrix at the same index and replace
                if(!validLValue(enhanced[j][i])){
                    for(int k = 0; k < llabMatrices.size(); k++){
                        if(k == baseIndex) continue;
                        double lValue = llabMatrices.get(k)[j][i];
                        if(validLValue(lValue)){
                            enhanced[j][i] = lValue;
                        }
                    }
                }
            }
        }
        return enhanced;
    }

    //returns a Llab matrix with the least amount of invalid pixels (<=20 or >=80)
    public double[][] FindLeastInvalidPixels(ArrayList<double[][]> llabMatrices) {
        double[][] leastInvalidPixelsMatrix = llabMatrices.get(0);

        int cols = llabMatrices.get(0)[0].length;
        int rows = llabMatrices.get(0).length;

        int lowestInvalidPixelCount = rows*cols;

        for (int i = 0; i < llabMatrices.size(); i++) {
            double[][] current = llabMatrices.get(i);
            int invalidCount = this.countInvalidPixels(current);

            if(invalidCount < lowestInvalidPixelCount) {
                lowestInvalidPixelCount = invalidCount;
                leastInvalidPixelsMatrix = current;
            }
        }
    return leastInvalidPixelsMatrix;
    }

    private boolean validLValue(double value){
        return !(value <= 20) && !(value >= 80);
    }

    public int countInvalidPixels(double[][] matrix){
        int count = 0;

        int cols = matrix[0].length;
        int rows = matrix.length;

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if(!validLValue(matrix[j][i])) count++;
            }
        }

        return count;
    }
}
