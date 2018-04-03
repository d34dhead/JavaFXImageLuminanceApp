package core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.*;

public class ConstructLuminanceMatrixForkTaskTest {
    private ImageProcessor processor;
    private BufferedImage srcImg;
    private ForkConstructLlabMatrix fork;
    private ForkJoinPool pool;
    private double[][] refLlabMatrix;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void compute() {
    }
}