package core;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ConstructLuminanceMatrixForkTask extends RecursiveTask<double[][]> {

    private ImageProcessor processor;
    private double[][] src;
    private double[][] dst;
    private int rows;
    private int cols;
    private int threadCount;
    private int startX;
    private int endX;
    private int startY;
    private int endY;
    private double fNumber;
    private double exposureTime;
    private double aCoeff;
    private double bCoeff;

    private ConstructLuminanceMatrixForkTask(ImageProcessor processor, double[][] src, double[][] dst, int rows, int cols, int startX,
                                             int endX, int startY, int endY, double fNumber, double exposureTime, double aCoeff, double bCoeff) {
        this.processor = processor;
        this.src = src;
        this.dst = dst;
        this.rows = rows;
        this.cols = cols;
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
        this.fNumber = fNumber;
        this.exposureTime = exposureTime;
        this.aCoeff = aCoeff;
        this.bCoeff = bCoeff;
    }

    public ConstructLuminanceMatrixForkTask(ImageProcessor processor, double[][] src, double fNumber, double exposureTime, double aCoeff, double bCoeff) {
        this.threadCount = Runtime.getRuntime().availableProcessors();
        this.processor = processor;
        this.src = src;
        this.rows = src.length;
        this.cols = src[0].length;
        this.dst = new double[rows][cols];
    }

    @Override
    protected double[][] compute() {
        //if called for the first time, sections not yet defined, need to split task
        if (endX == 0) {
            ForkJoinTask.invokeAll(createSubtasks());
            return dst;
        } else {
            return process();
        }
    }
    //divide and conquer
    private RecursiveTask[] createSubtasks() {
        //initialize empty matrix
        this.dst = new double[rows][cols];

        ConstructLuminanceMatrixForkTask[] tasks = new ConstructLuminanceMatrixForkTask[threadCount];
        //height divisible by threadCount -> divide vertically into n(=threadCount) sections
        if (rows % threadCount == 0) {
            startX = 0;
            endX = cols;
            int sectionHeight = rows / threadCount;

            for (int i = 0; i < threadCount; i++) {
                startY = i * sectionHeight;
                endY = (i + 1) * sectionHeight;
                tasks[i] = new ConstructLuminanceMatrixForkTask(processor, src, dst, rows, cols, startX, endX, startY, endY, fNumber, exposureTime, aCoeff, bCoeff);
            }
        } else if (cols % threadCount == 0) {
            startY = 0;
            endY = rows;
            int sectionWidth = cols / threadCount;

            for (int i = 0; i < threadCount; i++) {
                startX = i * sectionWidth;
                endX = (i + 1) * sectionWidth;
                tasks[i] = new ConstructLuminanceMatrixForkTask(processor, src, dst, rows, cols, startX, endX, startY, endY, fNumber, exposureTime, aCoeff, bCoeff);
            }
        } else {//no dimension divisible by threadCount -> last section is larger
            startX = 0;
            endX = cols;
            int sectionHeight = rows / threadCount;

            for (int i = 0; i < threadCount; i++) {
                startY = i * sectionHeight;
                //on the last iteration, process the portion that remains
                if (i == threadCount - 1) {
                    endY = rows;
                } else {
                    endY = (i + 1) * sectionHeight;
                }
                tasks[i] = new ConstructLuminanceMatrixForkTask(processor, src, dst, rows, cols, startX, endX, startY, endY, fNumber, exposureTime, aCoeff, bCoeff);
            }
        }
        return tasks;
    }

    private double[][] process() {
        return processor.constructLuminanceMatrix(src, dst, fNumber, exposureTime, aCoeff, bCoeff, startX, endX, startY, endY);
    }
}
