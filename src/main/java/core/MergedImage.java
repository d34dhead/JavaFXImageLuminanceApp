package core;

import java.util.List;

public class MergedImage implements MyImage {
    private final String imgName;
    private double [][] lLabMatrix;
    private double [][] luminanceMatrix;
    private List<UnmergedImage> sourceImages;
    private String resolution;

    public MergedImage(List<UnmergedImage> sourceImages){
        this.imgName = "Merged image";
        this.sourceImages = sourceImages;
        this.resolution = sourceImages.get(0).getResulution();
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
