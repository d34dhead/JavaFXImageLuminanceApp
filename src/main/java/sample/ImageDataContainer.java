package sample;

import java.awt.*;
import java.awt.image.BufferedImage;

/*Author: Lubomir Nepil*/
public class ImageDataContainer {
    private BufferedImage fullSizedImage;
    private BufferedImage resizedImg;
    private double[][] lMatrix;
    private double[][] lLabMatrix;
    private Double aperture;
    private Double exposure;
    //default color scale
    private Color[] hueImgColors = new Color[]{Color.BLACK, new Color(255, 0, 0),
            new Color(219, 70, 2), new Color(247, 220, 17), Color.WHITE};
    private String luminanceFormula;

    public String getLuminanceFormula() {
        return luminanceFormula;
    }

    public void setLuminanceFormula(String luminanceFormula) {
        this.luminanceFormula = luminanceFormula;
    }

    public Color[] getHueImgColors() {
        return hueImgColors;
    }

    public void setHueImgColors(Color[] hueImgColors) {
        this.hueImgColors = hueImgColors;
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

    public void setlLabMatrix(double[][] lLabMatrix) {
        this.lLabMatrix = lLabMatrix;
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

    public BufferedImage getResizedImg() {
        return resizedImg;
    }

    public void setResizedImg(BufferedImage resizedImg) {
        this.resizedImg = resizedImg;
    }

    public void populateLMatrix() {
        if (this.apertureAndExposureSet()) {
            if (this.lLabMatrix != null) {
                if (this.luminanceFormula == null) {
                    this.lMatrix = ImageProcessor.constructLuminanceMatrix(this.lLabMatrix, aperture, exposure);
                } else {
                    this.lMatrix = ImageProcessor.constructLuminanceMatrix(this.lLabMatrix, aperture, exposure, luminanceFormula);
                }
            }
        }
    }

    public void populateLlabMatrix(boolean resized) {

        if (!resized) {
            if (this.fullSizedImage != null) {
                this.lLabMatrix = ImageProcessor.constructLlabMatrix(this.fullSizedImage);
            }
        } else {
            if (this.resizedImg != null) {
                this.lLabMatrix = ImageProcessor.constructLlabMatrix(this.resizedImg);
            }
        }
    }

    public boolean apertureAndExposureSet() {
        return this.aperture != null && this.exposure != null;
    }


}
