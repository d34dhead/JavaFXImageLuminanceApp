package sample;
        /*Author: Lubomir Nepil*/
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageProcessor {
    /**
     * reference white in XYZ coordinates and conversion constants epsilon and ksi
     */
    private static double[] D50 = {0.964212, 0.1, 0.825188};
    private static double[] D55 = {0.956797, 0.1, 0.921481};
    private static double[] D65 = {0.950429, 0.1, 0.1088900};
    private static double[] D75 = {0.949722, 0.1, 0.1226394};
    private static double eps = 0.008856;
    private static double k = 903.3;

    //converts given argb pixel to Lab color system, returns a double array in the form [L, a, b]
    private static double[] convertPixelRgbToLab(int pixel) {
        Color color = new Color(pixel);
        return xyzToLab(rgbToXyz(color.getRed(), color.getGreen(), color.getBlue()));
    }

    //converts rgb values range <0,255> to xyz values <0,1>
    private static double[] rgbToXyz(int R, int G, int B) {

        //convert <0,255> to <0,1>
        double r = R / 255.f;
        double g = G / 255.f;
        double b = B / 255.f;
                /*apply sRGB -> XYZ conversion equations
                X, Y and Z output refer to a D65/2° standard illuminant.*/
        if (r <= 0.04045) {
            r = r / 12.92;
        } else {
            r = Math.pow(((r + 0.055) / 1.055), 2.4);
        }
        if (g <= 0.04045) {
            g = g / 12.92;
        } else {
            g = Math.pow(((g + 0.055) / 1.055), 2.4);
        }
        if (b <= 0.04045) {
            b = b / 12.92;
        } else {
            b = Math.pow(((b + 0.055) / 1.055), 2.4);
        }

        double[] result = new double[3];
        result[0] = r * 0.4124 + g * 0.3576 + b * 0.1805;
        result[1] = r * 0.2126 + g * 0.7152 + b * 0.0722;
        result[2] = r * 0.0193 + g * 0.1192 + b * 0.9505;
        return result;
    }

    private static double[] xyzToLab(double[] xyz) {

        //xyz already relative to D65
        double xr = xyz[0];
        double yr = xyz[1];
        double zr = xyz[2];

        double fx;
        double fy;
        double fz;

        //calculate Lab coords
        if (xr > eps) {
            fx = Math.pow(xr, 1.0 / 3.0);
        } else {
            fx = (k * xr + 16.0) / 116.0;
        }
        if (yr > eps) {
            fy = Math.pow(yr, 1.0 / 3.0);
        } else {
            fy = (k * yr + 16.0) / 116.0;
        }
        if (zr > eps) {
            fz = Math.pow(zr, 1.0 / 3.0);
        } else {
            fz = (k * zr + 16.0) / 116.0;
        }

        double[] result = new double[3];

        result[0] = (116.0 * fy) - 16.0;
        result[1] = 500.0 * (fx - fy);
        result[2] = 200.0 * (fy - fz);

        return result;

    }

    /*returns a matrix of L values in the range (0-100)
     *matrix is row major -> [rows][columns] */
    public static double[][] constructLlabMatrix(BufferedImage img) {
        int cols = img.getWidth();
        int rows = img.getHeight();
        double[][] LlabMatrix = new double[rows][cols];
        //fill matrix with Llab values
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                LlabMatrix[j][i] = convertPixelRgbToLab(img.getRGB(i, j))[0];
            }
        }
        return LlabMatrix;
    }

    /*This method outputs a matrix of luminance values from a matrix of Llab values and given aperture number and exposure time*/
    public static double[][] constructLuminanceMatrix(double[][] LlabMatrix, double aperture, double exposure) {
        int rows = LlabMatrix.length;
        int cols = LlabMatrix[0].length;

        //create an empty L matrix of the same size as Llab matrix
        double[][] luminanceMatrix = new double[rows][cols];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                luminanceMatrix[j][i] = (aperture * aperture / exposure) * 0.0373 * Math.exp(0.0307 * LlabMatrix[j][i]);
            }
        }
        return luminanceMatrix;
    }

    /*This method takes in the given image and outputs an image whose color is based
off the L value of each pixel in the Lab color system, as the L coordinate is proportional to Luminance.
Color scale can be changed in the double for loop.
It also outputs a matrix of Luminance values calculated from the L coordinate of each pixel in Lab color system.*/
    public static BufferedImage constructHueImage(double[][] LlabMatrix) {
        //get original img dimensions
        int height = LlabMatrix.length;
        int width = LlabMatrix[0].length;
        //construct an empty img with given dimensions
        BufferedImage hueImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //fill image by adding pixels one by one
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //get L value
                double lValue = LlabMatrix[j][i];
                //set pixel color based on L value
                if (lValue < 20) {
                    hueImg.setRGB(i, j, Color.BLACK.getRGB());
                } else if (lValue >= 20 && lValue < 30) {
                    hueImg.setRGB(i, j, new Color(163, 3, 3).getRGB());
                } else if (lValue >= 30 && lValue < 40) {
                    hueImg.setRGB(i, j, new Color(255, 0, 0).getRGB());
                } else if (lValue >= 40 && lValue < 50) {
                    hueImg.setRGB(i, j, new Color(219, 70, 2).getRGB());
                } else if (lValue >= 50 && lValue < 60) {
                    hueImg.setRGB(i, j, new Color(255, 136, 0).getRGB());
                } else if (lValue >= 60 && lValue < 70) {
                    hueImg.setRGB(i, j, new Color(255, 229, 0).getRGB());
                } else if (lValue >= 70 && lValue < 80) {
                    hueImg.setRGB(i, j, new Color(247, 220, 17).getRGB());
                } else {
                    hueImg.setRGB(i, j, Color.WHITE.getRGB());
                }
            }
        }

        return hueImg;
    }

}
