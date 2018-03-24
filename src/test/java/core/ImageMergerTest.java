package core;

import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ImageMergerTest {

    private ImageMerger merger;
    private ArrayList<double[][]> invalidPixelTestMatrices;
    private ArrayList<double[][]> mergeTestMatrices;
    private BufferedImage[] images;
    private double[][] llabMatrix;

    @Before
    public void setUp() throws Exception {
        merger = new ImageMerger(new ImageProcessor());
        llabMatrix = new double[][]{{50, 50, 50}, {5, 6, 80}, {90, 80, 50}};

        invalidPixelTestMatrices = new ArrayList<>();

        for(int i = 0; i < 10 ; i++){
            invalidPixelTestMatrices.add(new double[][]{{50 * i, 50, 50}, {5, 6, 80}, {90, 70, 50}});
        }

        mergeTestMatrices = new ArrayList<>();
        mergeTestMatrices.add(new double[][]{{50, 0, 80, 90},{55, 66, 79, 40},{80, 85, 40, 5}});
        mergeTestMatrices.add(new double[][]{{50, 55, 13, 60},{55, 12, 85, 40},{80, 55, 40, 5}});
        mergeTestMatrices.add(new double[][]{{0, 0, 80, 90},{20, 77, 79, 40},{80, 85, 40, 5}});
        mergeTestMatrices.add(new double[][]{{80, 44, 5, 90},{8, 11, 79, 40},{80, 85, 40, 5}});



    }

    @Test
    public void FindLeastInvalidPixels() {
        assertEquals(merger.countInvalidPixels(new double[][]{{50, 50, 50}, {5, 6, 80}, {90, 70, 50}}), merger.countInvalidPixels(merger.FindLeastInvalidPixels(invalidPixelTestMatrices)), .01);
    }

    @Test
    public void countInvalidPixels() {
            assertEquals(5, merger.countInvalidPixels(llabMatrix));
    }

    @Test
    public void mergeLlabMatrices() {
        int beforeEnhancing = merger.countInvalidPixels(mergeTestMatrices.get(0));
        int afterEnhancing = merger.countInvalidPixels(merger.MergeLlabMatrices(0,mergeTestMatrices ));

        assertEquals(6, beforeEnhancing);
        assertEquals(3, afterEnhancing);
    }

}