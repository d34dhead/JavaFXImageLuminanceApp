package core;


import java.awt.image.BufferedImage;

public class UnmergedImage {
    private final String imgName;
    private final BufferedImage img;
    private final double exposureTime;
    private final double fNumber;
    private final String resulution;
    private double [][] lLabMatrix;
    private double [][] luminanceMatrix;

    public UnmergedImage(BufferedImage img, double exposureTime, double fNumber, String imgName){
        this.img = img;
        this.exposureTime = exposureTime;
        this.fNumber = fNumber;
        this.imgName = imgName;
        this.resulution = img.getWidth() + "x" + img.getHeight();
    }

    public String getResulution() {
        return resulution;
    }

    public String getImgName() {
        return imgName;
    }

    public BufferedImage getImg() {
        return img;
    }

    public double getExposureTime() {
        return exposureTime;
    }

    public double getfNumber() {
        return fNumber;
    }

    public void setlLabMatrix(double[][] lLabMatrix) {
        this.lLabMatrix = lLabMatrix;
    }

    public void setLuminanceMatrix(double[][] luminanceMatrix) {
        this.luminanceMatrix = luminanceMatrix;
    }

    public double[][] getlLabMatrix() {
        return lLabMatrix;
    }

    public double[][] getLuminanceMatrix() {
        return luminanceMatrix;
    }

    public boolean isInitialized(){
        return this.lLabMatrix != null && this.luminanceMatrix != null;
    }

}
