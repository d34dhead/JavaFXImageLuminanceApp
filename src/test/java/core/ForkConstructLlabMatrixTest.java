package core;

import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.*;

public class ForkConstructLlabMatrixTest {
    private ImageProcessor processor;
    private BufferedImage srcImg;
    private ForkConstructLlabMatrix fork;
    private ForkJoinPool pool;
    private double[][] refLlabMatrix;

    @Before
    public void setUp() throws Exception {
        processor = new ImageProcessor();
        pool = new ForkJoinPool();
        srcImg = ImageIO.read(new File("5692x5594.jpg"));
        fork = new ForkConstructLlabMatrix(processor, srcImg);
        //long starttime = System.nanoTime();
        refLlabMatrix = processor.constructLlabMatrix(srcImg);
        //long timeElapsed = System.nanoTime() - starttime;
        //System.out.println("Time elapsed single threaded: " + timeElapsed);
    }

    @Test
    public void correctThreadCount(){
        assertEquals(pool.getParallelism(),  Runtime.getRuntime().availableProcessors());
    }

    @Test
    public void taskNotNull(){
        assertNotNull(fork);
    }

    @Test
    public void compute() {
        long starttime = System.nanoTime();
        pool.execute(fork);
        double[][] llabMatrix = fork.join();
        long timeElapsed = System.nanoTime() - starttime;
        System.out.println("Time elapsed multithreaded: " + timeElapsed);
        assertNotNull(llabMatrix);
        assertEquals(srcImg.getWidth(), llabMatrix[0].length);
        assertEquals(srcImg.getHeight(), llabMatrix.length);

        for (int i = 0; i < srcImg.getWidth(); i++) {
            for (int j = 0; j < srcImg.getHeight(); j++) {
                assertEquals(refLlabMatrix[j][i], llabMatrix[j][i], .1);
            }
        }
    }

}