package core;


import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.stream.DoubleStream;

public class UnmergedImage implements MyImage {
    private final String imgName;
    private final BufferedImage img;
    private final double exposureTime;
    private final double fNumber;
    private final String resulution;
    private double [][] lLabMatrix;
    private double [][] luminanceMatrix;
    private boolean selected;
    private double maxLuminance;
    private double minLuminance;

    public UnmergedImage(BufferedImage img, double exposureTime, double fNumber, String imgName){
        this.img = img;
        this.exposureTime = exposureTime;
        this.fNumber = fNumber;
        this.imgName = imgName;
        this.resulution = img.getWidth() + "x" + img.getHeight();
        this.selected = false;
    }
    public double getMaxLuminance() {
        if(maxLuminance == 0){
            return this.findMaxLuminance();
        }
        return maxLuminance;
    }

    public double getMinLuminance() {
        if(minLuminance == 0){
            return this.findMinLuminance();
        }
        return minLuminance;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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

    private double findMaxLuminance() {
        if(isInitialized()){
            DoubleStream stream = Arrays.stream(luminanceMatrix).flatMapToDouble(Arrays::stream).filter(d -> (d != 0 && d != 9999));
            return maxLuminance = stream.max().getAsDouble();
        }
        return 0;
    }

    private double findMinLuminance() {
        if(isInitialized()){
            DoubleStream stream = Arrays.stream(luminanceMatrix).flatMapToDouble(Arrays::stream).filter(d -> (d != 0 && d != 9999));
            return minLuminance = stream.min().getAsDouble();
        }
        return 0;
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

    @Override
    public boolean isInitialized(){
        return this.lLabMatrix != null && this.luminanceMatrix != null;
    }

}
