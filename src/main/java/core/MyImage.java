package core;

public interface MyImage {

    void setLuminanceMatrix(double[][] luminanceMatrix);
    void setlLabMatrix(double[][] lLabMatrix);
    double getMaxLuminance();
    double getMinLuminance();

    String getImgName();
    String getResulution();
    double[][] getLuminanceMatrix();
    double[][] getlLabMatrix();

    boolean isInitialized();
}
