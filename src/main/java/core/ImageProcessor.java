package core;
        /*Author: Lubomir Nepil*/

import org.mariuszgromada.math.mxparser.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.RecursiveAction;

public class ImageProcessor{

    private static double eps = 0.008856;
    private static double k = 903.3;

    //converts given argb pixel to Lab color system, returns a double array in the form [L, a, b]
    public double convertPixelRgbToLab(int pixel) {
        //
        Color color = new Color(pixel);
        //
        return xyzToLab(rgbToXyz(color.getRed(), color.getGreen(), color.getBlue()));
    }

    //converts rgb values range <0,255> to xyz values <0,1>
    public double rgbToXyz(int R, int G, int B) {

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

        double resultY;
        resultY = r * 0.2126 + g * 0.7152 + b * 0.0722;
        return resultY;
    }
    //
    public double xyzToLab(double resultY) {
        //xyz already relative to D65
        double fy;

        //calculate L coordinate
        if (resultY > eps) {
            fy = Math.pow(resultY, 1.0 / 3.0);
        } else {
            fy = (k * resultY + 16.0) / 116.0;
        }

        double result;

        result = (116.0 * fy) - 16.0;

        return result;

    }

    /*returns a matrix of L values in the range (0-100)
     *matrix is row major -> [rows][columns] */
    public double[][] constructLlabMatrix(BufferedImage img) {
        int cols = img.getWidth();
        int rows = img.getHeight();
        double[][] LlabMatrix = new double[rows][cols];
        //fill matrix with Llab values
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                LlabMatrix[j][i] = convertPixelRgbToLab(img.getRGB(i, j));
            }
        }
        return LlabMatrix;
    }
    //process only a portion of the image
    public double[][] constructLlabMatrix(BufferedImage img, double[][] dst, int startX, int endX, int startY, int endY) {
        //fill matrix with Llab values
        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                dst[j][i] = convertPixelRgbToLab(img.getRGB(i, j));
            }
        }
        return dst;
    }

    /*This method outputs a matrix of luminance values from a matrix of Llab values and given aperture number and exposure time
     * Faster than the same method with specified formula*/
    public double[][] constructLuminanceMatrix(double[][] LlabMatrix, double aperture, double exposure) {

        int rows = LlabMatrix.length;
        int cols = LlabMatrix[0].length;

        //create an empty L matrix of the same size as Llab matrix
        double[][] luminanceMatrix = new double[rows][cols];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                //Llab values <= 20 && >= 80 not taken into account
                if (LlabMatrix[j][i] <= 20) {
                    luminanceMatrix[j][i] = 0;
                } else if (LlabMatrix[j][i] >= 80) {
                    luminanceMatrix[j][i] = 9999;
                } else {
                    luminanceMatrix[j][i] = (aperture * aperture / exposure) * 0.0373 * Math.exp(0.0307 * LlabMatrix[j][i]);
                }
            }
        }
        return luminanceMatrix;
    }
    //overloaded method with formula coefficients input instead of the whole formula, as it is unlikely the equation itself will change
    public double[][] constructLuminanceMatrix(double[][] LlabMatrix, double aperture, double exposure, double Acoeff, double Bcoeff) {

        int rows = LlabMatrix.length;
        int cols = LlabMatrix[0].length;

        //create an empty L matrix of the same size as Llab matrix
        double[][] luminanceMatrix = new double[rows][cols];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                //Llab values <= 20 && >= 80 not taken into account
                if (LlabMatrix[j][i] <= 20) {
                    luminanceMatrix[j][i] = 0;
                } else if (LlabMatrix[j][i] >= 80) {
                    luminanceMatrix[j][i] = 9999;
                } else {
                    luminanceMatrix[j][i] = (aperture * aperture / exposure) * Acoeff * Math.exp(Bcoeff * LlabMatrix[j][i]);
                }
            }
        }
        return luminanceMatrix;
    }
    //overloaded method for processing only a part of an image with bounds specified
    public double[][] constructLuminanceMatrix(double[][] LlabMatrix, double[][] dstLuminanceMatrix, double aperture, double exposure, double Acoeff, double Bcoeff, int startX, int endX, int startY, int endY) {

        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                //Llab values <= 20 && >= 80 not taken into account
                if (LlabMatrix[j][i] <= 20) {
                    dstLuminanceMatrix[j][i] = 0;
                } else if (LlabMatrix[j][i] >= 80) {
                    dstLuminanceMatrix[j][i] = 9999;
                } else {
                    dstLuminanceMatrix[j][i] = (aperture * aperture / exposure) * Acoeff * Math.exp(Bcoeff * LlabMatrix[j][i]);
                }
            }
        }
        return dstLuminanceMatrix;
    }

    //Overloaded method with formula input
    public double[][] constructLuminanceMatrix(
            double[][] LlabMatrix, double aperture, double exposure, String expressionString) {
        //define function arguments
        Argument fNumber = new Argument("F", aperture);
        Argument exposureTime = new Argument("t", exposure);
        //dependent argument L
        Argument lightness = new Argument("Llab");
        //expression used to calculate luminance
        Expression luminanceFormula = new Expression(expressionString, fNumber, exposureTime, lightness);

        int rows = LlabMatrix.length;
        int cols = LlabMatrix[0].length;

        //create a temp L matrix of the same size as Llab matrix
        double[][] luminanceMatrix = new double[rows][cols];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                lightness.setArgumentValue(LlabMatrix[j][i]);
                luminanceMatrix[j][i] = luminanceFormula.calculate();
            }
        }
        return luminanceMatrix;
    }

    /*This method takes in the given image and outputs an image whose color is based
    off the L value of each pixel in the Lab color system, as the L coordinate is proportional to Luminance.
    Color scale can be changed by Color array input when invoking the method.*/
    public BufferedImage constructHueImage(double[][] LlabMatrix, Color[] colors) {
        double intervalSize = 100.f / colors.length;
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
                Color color;
                //set pixel color based on L value
                if (lValue != 100) {
                    color = colors[(int) Math.floor(lValue / intervalSize)];
                } else {
                    color = Color.WHITE;
                }

                hueImg.setRGB(i, j, color.getRGB());
            }
        }
        return hueImg;
    }
}
