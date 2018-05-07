package core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

public class MergedImage implements MyImage {
    private final String imgName;
    private double [][] lLabMatrix;
    private double [][] luminanceMatrix;
    private String resolution;
    private double maxLuminance;
    private double minLuminance;

    public MergedImage(List<UnmergedImage> sourceImages){
        this.imgName = "Merged image";
        this.resolution = sourceImages.get(0).getResulution();
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

    public String getImgName() {
        return imgName;
    }

    @Override
    public String getResulution() {
        return this.resolution;
    }

    public double[][] getlLabMatrix() {
        return lLabMatrix;
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

    public double[][] getLuminanceMatrix() {
        return luminanceMatrix;
    }

    public void setLuminanceMatrix(double[][] luminanceMatrix) {
        this.luminanceMatrix = luminanceMatrix;
    }

    @Override
    public boolean isInitialized() {
        return this.lLabMatrix != null && this.luminanceMatrix != null;
    }
}
