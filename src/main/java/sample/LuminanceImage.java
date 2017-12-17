package sample;
/*Author: Lubomir Nepil*/
public class LuminanceImage {
    private double[][] lMatrix;
    private double[][] lLabMatrix;
    private Double aperture;
    private Double exposure;

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

    public double getPixelLuminance(int x, int y) {

        System.out.println("Array width: " + lMatrix[0].length);
        System.out.print("Acessing value x: " + x + " y: " + y);
        return this.lMatrix[y][x];
    }

    public void populateLMatrix() {
        if(this.lLabMatrix != null) {
            this.lMatrix = ImageProcessor.constructLuminanceMatrix(this.lLabMatrix, aperture, exposure);
        }
    }

    public boolean apertureAndExposureSet(){
        if(this.aperture != null && this.exposure != null){
            return true;

        }
        return false;
    }
}
