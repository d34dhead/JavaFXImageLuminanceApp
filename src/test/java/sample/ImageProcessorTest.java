package sample;

import core.ImageProcessor;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.Assert.*;

public class ImageProcessorTest {
    private ImageProcessor processor;
    private BufferedImage srcImg;
    private Color[] testColors;
    private int[] argbIntColors;
    private double[] expectedYs;
    private double[] expectedLs;

    @Before
    public void setUp() throws Exception {
        processor = new ImageProcessor();
        srcImg = ImageIO.read(new File("brooke-lark-391764.jpg"));
        testColors = new Color[]{new Color(120,100,180), new Color( 200, 120, 50 )
                , new Color( 100, 120, 50 ), new Color( 120, 250, 180 ), new Color( 5, 80, 140 )};
        expectedYs = new double[]{0.1640, 0.2595, 0.1637, 0.7566, 0.0766};
        expectedLs = new double[]{47.50, 57.99, 47.46, 89.70, 33.27};

        argbIntColors = new int[testColors.length];
        for(int i = 0; i < testColors.length; i++){
              argbIntColors[i] = testColors[i].getRGB();
        }
    }

    @Test
    public void convertPixelRgbToLab() {
        double[] results = new double[testColors.length];
        for(int i = 0; i < testColors.length; i++){
           results[i] = processor.convertPixelRgbToLab(argbIntColors[i]);
        }
        assertArrayEquals(expectedLs, results, 0.01);
    }

    @Test
    public void rgbToXyz() {
        double[] results = new double[testColors.length];
        for(int i = 0; i < testColors.length; i++){
            Color current = testColors[i];
            results[i] = processor.rgbToXyz(current.getRed(), current.getGreen(), current.getBlue());
        }
        assertArrayEquals(expectedYs, results, 0.0001);
    }

    @Test
    public void xyzToLab() {
        double[] results = new double[expectedYs.length];
        for(int i = 0; i < expectedYs.length; i++){
            double currentY = expectedYs[i];
            results[i] = processor.xyzToLab(currentY);
        }
        assertArrayEquals(expectedLs, results, .01);

    }

    @Test
    public void constructLlabMatrix() {
        double[][] LlabMatrix = processor.constructLlabMatrix(srcImg);
        assertNotNull(LlabMatrix);
        assertEquals(srcImg.getHeight(), LlabMatrix.length);
        assertEquals(srcImg.getWidth(), LlabMatrix[0].length);

        for (int i = 0; i < srcImg.getWidth(); i++) {
            for (int j = 0; j < srcImg.getHeight(); j++) {
                assertEquals(processor.convertPixelRgbToLab(srcImg.getRGB(i, j)), LlabMatrix[j][i], .1);
            }
        }
    }

    @Test
    public void constructLuminanceMatrix() {
        double[][] LlabMatrix = processor.constructLlabMatrix(srcImg);
        double[][] LMatrix = processor.constructLuminanceMatrix(LlabMatrix, 4, 0.05);

        assertNotNull(LMatrix);
        assertEquals(LlabMatrix.length, LMatrix.length);
        assertEquals(LlabMatrix[0].length, LMatrix[0].length);

    }

    @Test
    public void constructLlabMatrix1() {
        double[][] LlabMatrix = processor.constructLlabMatrix(srcImg,new double[srcImg.getHeight()][srcImg.getWidth()], 0, srcImg.getWidth(), 0, srcImg.getHeight());
        assertNotNull(LlabMatrix);
        assertEquals(srcImg.getHeight(), LlabMatrix.length);
        assertEquals(srcImg.getWidth(), LlabMatrix[0].length);

        for (int i = 0; i < srcImg.getWidth(); i++) {
            for (int j = 0; j < srcImg.getHeight(); j++) {
                assertEquals(processor.convertPixelRgbToLab(srcImg.getRGB(i, j)), LlabMatrix[j][i], .1);
            }
        }
    }
}