package core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.*;

public class ConstructLuminanceMatrixForkTaskTest {
    private ImageProcessor processor;
    private BufferedImage srcImg;
    private ConstructLuminanceMatrixForkTask forkTask;
    private ForkJoinPool pool;
    private double[][] refLlabMatrix;
    private double[][] refLuminanceMatrix;
    private double[][] dstLuminanceMatrix;
    private double fNumber = 4;
    private double exposureTime = .05;
    private double aCoeff = 0.0373;
    private double bCoeff = 0.0307;

    @Before
    public void setUp() throws Exception {
        pool = new ForkJoinPool();
        processor = new ImageProcessor();
        srcImg = ImageIO.read(new File("5692x5594.jpg"));
        refLlabMatrix = processor.constructLlabMatrix(srcImg);
        dstLuminanceMatrix = new double[refLlabMatrix.length][refLlabMatrix[0].length];
        long starttime = System.nanoTime();
        refLuminanceMatrix = processor.constructLuminanceMatrix(refLlabMatrix, dstLuminanceMatrix, fNumber, exposureTime, aCoeff, bCoeff, 0, refLlabMatrix[0].length, 0, refLlabMatrix.length);
        long timeElapsed = System.nanoTime() - starttime;
        System.out.println("Time elapsed single threaded: " + timeElapsed);
        forkTask = new ConstructLuminanceMatrixForkTask(processor, refLlabMatrix, fNumber, exposureTime, aCoeff, bCoeff);
    }

    @After
    public void tearDown() throws Exception {
        forkTask = null;
        refLuminanceMatrix = null;
        pool = null;
        dstLuminanceMatrix = null;
    }

    @Test
    public void verifyImgProcessorLuminanceCalculationMethodsConsistency(){

        double[][] vanillaLuminanceMatrix = processor.constructLuminanceMatrix(refLlabMatrix, fNumber, exposureTime);
        double[][] luminanceMatrixWithBounds = processor.constructLuminanceMatrix(refLlabMatrix, dstLuminanceMatrix, fNumber, exposureTime, aCoeff, bCoeff, 0, refLlabMatrix[0].length, 0, refLlabMatrix.length);

        for (int i = 0; i < srcImg.getWidth(); i++) {
            for (int j = 0; j < srcImg.getHeight(); j++) {
                assertEquals(vanillaLuminanceMatrix[j][i], luminanceMatrixWithBounds[j][i], .1);
            }
        }
    }

    @Test
    public void compute() {

        long starttime = System.nanoTime();
        pool.execute(forkTask);
        double[][] result = forkTask.join();
        long timeElapsed = System.nanoTime() - starttime;
        System.out.println("Time elapsed multithreaded: " + timeElapsed);
        assertNotNull(result);
        assertEquals(srcImg.getWidth(), result[0].length);
        assertEquals(srcImg.getHeight(), result.length);

        for (int i = 0; i < srcImg.getWidth(); i++) {
            for (int j = 0; j < srcImg.getHeight(); j++) {
                assertEquals(refLuminanceMatrix[j][i], result[j][i], .1);
            }
        }
    }


}