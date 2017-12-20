package sample;
/*Author: Lubomir Nepil*/
import com.mortennobel.imagescaling.MultiStepRescaleOp;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageScaler {

    public static BufferedImage rescale(BufferedImage src ,int dstWidth, int dstHeight){
        MultiStepRescaleOp rescaleOp = new MultiStepRescaleOp(dstWidth, dstHeight);

        return rescaleOp.doFilter(src, null, dstWidth, dstHeight);
    }
}
