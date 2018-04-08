package core;

import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ImageMergerTest {

    private ImageDataCache cache;
    private ImageMerger merger;
    private List<UnmergedImage> imageList;



    @Before
    public void setUp(){
        merger = new ImageMerger(new ImageProcessor(), ImageDataCache.getInstance());



    }

    @Test
    public void mergeLlabMatrices() {
    }
}