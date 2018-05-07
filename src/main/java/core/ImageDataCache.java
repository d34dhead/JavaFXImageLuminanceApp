package core;

import com.drew.lang.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/*Author: Lubomir Nepil*/
public class ImageDataCache {
    private static ImageDataCache instance = null;

    private List<UnmergedImage> imageList;
    private MergedImage mergedImage;
    private PropertiesManager props;
    private double[][] lMatrix;

    private ImageProcessor processor = new ImageProcessor();
    private ImageMerger merger = new ImageMerger(processor, this);

    private ConstructLlabMatrixForkTask llabForkTask;
    private ConstructLuminanceMatrixForkTask luminanceForkTask;
    private ForkJoinPool pool;

    //default color scale
    private Color[] hueImgColors = new Color[]{Color.RED, new Color(255, 89, 0),
            new Color(255, 140, 0), new Color(255, 187, 0), Color.YELLOW};
    private String luminanceFormula;

    private ImageDataCache(){
        this.imageList = new ArrayList<>();
        this.pool = new ForkJoinPool();
    }

    public static ImageDataCache getInstance(){
        if(instance == null) {
            instance = new ImageDataCache();
        }
        return instance;
    }

    public void initializeImageMatrices(MyImage myImg, boolean coeffsChanged){
        UnmergedImage img = (UnmergedImage) myImg;
        if(img.getlLabMatrix() == null) {
            this.llabForkTask = new ConstructLlabMatrixForkTask(this.processor, img.getImg());
            pool.execute(llabForkTask);
            img.setlLabMatrix(llabForkTask.join());
        }
        //if coefficients changed, need to recalculate luminance, thus the coeffChange boolean
        if(img.getLuminanceMatrix() == null || coeffsChanged) {
            props = new PropertiesManager();

            if (this.luminanceFormula == null) {
                if (props.containsKey("coefficientA") && props.containsKey("coefficientB")) {
                    double coeffA = Double.parseDouble(props.getProperty("coefficientA"));
                    double coeffB = Double.parseDouble(props.getProperty("coefficientB"));
                        this.luminanceForkTask = new ConstructLuminanceMatrixForkTask(processor, img.getlLabMatrix(), img.getfNumber(), img.getExposureTime(), coeffA, coeffB);
                        pool.execute(luminanceForkTask);
                        img.setLuminanceMatrix(luminanceForkTask.join());
                } else {
                    this.luminanceForkTask = new ConstructLuminanceMatrixForkTask(processor, img.getlLabMatrix(), img.getfNumber(), img.getExposureTime(), 0.0373, 0.0307);
                    pool.execute(luminanceForkTask);
                    img.setLuminanceMatrix(luminanceForkTask.join());
                }
            } else {
                this.lMatrix = processor.constructLuminanceMatrix(img.getlLabMatrix(), img.getfNumber(), img.getExposureTime(), luminanceFormula);
            }

            props = null;

        }
    }

    public List<UnmergedImage> getSelectedImages(){
        return this.imageList.stream()
                .filter(UnmergedImage::isSelected)
                .collect(Collectors.toList());
    }

    public List<UnmergedImage> getImageList() {
        return imageList;
    }

    public ImageProcessor getProcessor() {
        return processor;
    }

    public void setLuminanceFormula(String luminanceFormula) {
        this.luminanceFormula = luminanceFormula;
    }

    public Color[] getHueImgColors() {
        return hueImgColors;
    }

    public MergedImage mergeAllImages(){
            return this.mergedImage = merger.MergeImages(this.imageList);
    }

    public boolean imgDimensionsEqual() {
        boolean allEqual = true;
        for (int i = 0; i < imageList.size() - 1; i++) {
            int width1 = imageList.get(i).getImg().getWidth();
            int width2 = imageList.get(i+1).getImg().getWidth();
            int height1 = imageList.get(i).getImg().getHeight();
            int height2 = imageList.get(i+1).getImg().getHeight();

            if (width1 != width2 || height1 != height2) {
                allEqual = false;
                break;
            }
        }
        return allEqual;
    }


}
