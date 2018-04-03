package core;


import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;

public class UnmergedImage {
    private final String imgName;
    private final BufferedImage img;
    private final double exposureTime;
    private final double fNumber;
    private double [][] lLabMatrix;
    private double [][] luminanceMatrix;

    public UnmergedImage(BufferedImage img, double exposureTime, double fNumber, String imgName){
        this.img = img;
        this.exposureTime = exposureTime;
        this.fNumber = fNumber;
        this.imgName = imgName;
        this.lLabMatrix = new double[img.getHeight()][img.getWidth()];
        this.luminanceMatrix = new double[img.getHeight()][img.getWidth()];
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

    public double[][] getlLabMatrix() {
        return lLabMatrix;
    }

    public double[][] getLuminanceMatrix() {
        return luminanceMatrix;
    }
}
