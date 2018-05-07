package core;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorMapper {
    private static ImageDataCache cache = ImageDataCache.getInstance();

    //determine intervals
    public static BufferedImage reconstructImage(Color[] colors, MyImage image){
        if(!image.isInitialized()) {
            cache.initializeImageMatrices(image, false);
        }

        double maxL = image.getMaxLuminance();
        double minL = image.getMinLuminance();
        double intervalSize = (maxL - minL) / colors.length;

        double[][] llabMatrix = image.getlLabMatrix();
        double[][] luminanceMatrix = image.getLuminanceMatrix();

        int height = image.getlLabMatrix().length;
        int width = image.getlLabMatrix()[0].length;

        //construct an empty img with given dimensions
        BufferedImage reconstructedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

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
                } else if(llab <= 20) {
                    color = Color.BLACK;
                } else if(luminance < minL + intervalSize){
                    color = colors[0];
                } else if(luminance < minL + 2 * intervalSize){
                    color = colors[1];
                } else if(luminance < minL + 3 * intervalSize){
                    color = colors[2];
                } else if(luminance < minL + 4 * intervalSize){
                    color = colors[3];
                } else {
                    color = colors[4];
                }

                reconstructedImg.setRGB(i, j, color.getRGB());
            }
        }
        return reconstructedImg;
    }
}

