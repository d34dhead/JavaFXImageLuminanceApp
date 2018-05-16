package core;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class ReconstructedImage extends BufferedImage {

    public static Color[] DEFAULT_COLORS = new Color[]{Color.YELLOW, new Color(255, 187, 0),
            new Color(255, 140, 0), new Color(255, 89, 0), Color.RED};
    private double[] luminanceThreshholds;
    private Color[] colors;

    public ReconstructedImage(int width, int height, int imageType) {
        super(width, height, imageType);
        this.colors = DEFAULT_COLORS;
    }

    public ReconstructedImage(int width, int height, int imageType, IndexColorModel cm) {
        super(width, height, imageType, cm);
        this.colors = DEFAULT_COLORS;
    }

    public ReconstructedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
        super(cm, raster, isRasterPremultiplied, properties);
        this.colors = DEFAULT_COLORS;
    }

    public ReconstructedImage(int width, int height, int imageType, Color[] colors) {
        super(width, height, imageType);
        this.colors = colors;
    }

    public ReconstructedImage(int width, int height, int imageType, Color[] colors, double[] luminanceThreshholds) {
        super(width, height, imageType);
        this.colors = colors;
        this.luminanceThreshholds = luminanceThreshholds;
    }

    public static Color[] getDefaultColors() {
        return DEFAULT_COLORS;
    }

    public double[] getLuminanceThreshholds() {
        return luminanceThreshholds;
    }

    public Color[] getColors() {
        if(colors == null){
            return DEFAULT_COLORS;
        }
        return colors;
    }

    public void setLuminanceThreshholds(double[] luminanceThreshholds) {
        this.luminanceThreshholds = luminanceThreshholds;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }
}
