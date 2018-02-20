package sample;

import java.awt.*;
import java.awt.image.BufferedImage;

/*Author: Lubomir Nepil*/
public class ImageDataBuffer {
    private BufferedImage fullSizedImage;
    private BufferedImage resizedImg;
    private double[][] lMatrix;
    private double[][] lLabMatrix;
    private Double aperture;
    private Double exposure;
    private ImageProcessor processor = new ImageProcessor();
    //default color scale
    private Color[] hueImgColors = new Color[]{Color.BLACK, new Color(255, 0, 0),
            new Color(219, 70, 2), new Color(247, 220, 17), Color.WHITE};
    private String luminanceFormula;

    public ImageProcessor getProcessor() {
        return processor;
    }

    public String getLuminanceFormula() {
        return luminanceFormula;

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
        if (this.apertureAndExposureSet()) {
            if (this.lLabMatrix != null) {
                if (this.luminanceFormula == null) {
                    this.lMatrix = processor.constructLuminanceMatrix(this.lLabMatrix, aperture, exposure);
                } else {
                    this.lMatrix = processor.constructLuminanceMatrix(this.lLabMatrix, aperture, exposure, luminanceFormula);
                }
            }
        }
    }

    public void populateLlabMatrix(boolean resized) {

        if (!resized) {
            if (this.fullSizedImage != null) {
                this.lLabMatrix = processor.constructLlabMatrix(this.fullSizedImage);
            }
        } else {
            if (this.resizedImg != null) {
                this.lLabMatrix = processor.constructLlabMatrix(this.resizedImg);
            }
        }
    }

    public boolean apertureAndExposureSet() {
        return this.aperture != null && this.exposure != null;
    }


}
