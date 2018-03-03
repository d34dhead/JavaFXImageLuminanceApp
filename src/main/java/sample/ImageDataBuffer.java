package sample;

import java.awt.*;
import java.awt.image.BufferedImage;

/*Author: Lubomir Nepil*/
public class ImageDataBuffer {
    private PropertiesManager props;
    private BufferedImage[] images;
    private BufferedImage fullSizedImage;
    private BufferedImage resizedImg;
    private double[][] lMatrix;
    private double[][] lLabMatrix;
    private Double aperture;
    private Double exposure;
    private ImageProcessor processor = new ImageProcessor();
    private ImageMerger merger = new ImageMerger(processor);
    //default color scale
    private Color[] hueImgColors = new Color[]{Color.BLACK, new Color(255, 0, 0),
            new Color(219, 70, 2), new Color(247, 220, 17), Color.WHITE};
    private String luminanceFormula;

    public ImageProcessor getProcessor() {
        return processor;
    }

    public BufferedImage[] getImages() {
        return images;
    }

    public void setImages(BufferedImage[] images) {
        this.images = images;
    }


    public void setLuminanceFormula(String luminanceFormula) {
        this.luminanceFormula = luminanceFormula;
    }

    public Color[] getHueImgColors() {
        return hueImgColors;
    }

    public void setAperture(double aperture) {
        this.aperture = aperture;
    }

    public void setExposure(double exposure) {
        this.exposure = exposure;
    }

    public double[][] getlLabMatrix() {
        return lLabMatrix;
    }

    public double[][] getlMatrix() {
        return lMatrix;
    }

    public BufferedImage getFullSizedImage() {
        return fullSizedImage;
    }

    public void setFullSizedImage(BufferedImage fullSizedImage) {
        this.fullSizedImage = fullSizedImage;
    }

    public double getPixelLuminance(int x, int y) {
        return this.lMatrix[y][x];
    }

    public void setResizedImg(BufferedImage resizedImg) {
        this.resizedImg = resizedImg;
    }

    public void populateLMatrix() {
        props = new PropertiesManager();
        if (this.apertureAndExposureSet()) {
            if (this.lLabMatrix != null) {
                if (this.luminanceFormula == null) {
                    if(props.containsKey("coefficientA") && props.containsKey("coefficientB")) {
                        double coeffA = Double.parseDouble(props.getProperty("coefficientA"));
                        double coeffB = Double.parseDouble(props.getProperty("coefficientB"));
                        System.out.println("A, B : " + coeffA + ", " + coeffB);
                        this.lMatrix = processor.constructLuminanceMatrix(this.lLabMatrix, aperture,
                                exposure, coeffA, coeffB);
                    }else {
                        this.lMatrix = processor.constructLuminanceMatrix(this.lLabMatrix, aperture, exposure);
                    }
                }else{
                    this.lMatrix = processor.constructLuminanceMatrix(this.lLabMatrix, aperture, exposure, luminanceFormula);
                }
            }
        }
        props = null;
    }

    public void populateLlabMatrix(boolean resized) {

        if(this.images == null) {
            if (!resized) {
                if (this.fullSizedImage != null) {
                    this.lLabMatrix = processor.constructLlabMatrix(this.fullSizedImage);
                }
            } else {
                if (this.resizedImg != null) {
                    this.lLabMatrix = processor.constructLlabMatrix(this.resizedImg);
                }
            }
        } else {
            this.lLabMatrix = merger.MergeImages(images);
        }

    }

    public boolean apertureAndExposureSet() {
        return this.aperture != null && this.exposure != null;
    }


}
