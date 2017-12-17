package sample;
/*Author: Lubomir Nepil*/
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageScaler {



    public static BufferedImage scaleImage(BufferedImage srcImg, int width, int height) {

        int imgWidth = srcImg.getWidth();
        int imgHeight = srcImg.getHeight();

        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.clearRect(0, 0, width, height);
            g.drawImage(srcImg, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return newImage;
    }

}
