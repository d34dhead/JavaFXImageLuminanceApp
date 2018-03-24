package sample;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class ForkConstructLlabMatrix extends RecursiveTask<double[][]> {

    private ImageProcessor processor;
    private BufferedImage src;
    private double[][] dst;
    private int rows;
    private int cols;
    private int threadCount;
    private int startX;
    private int endX;
    private int startY;
    private int endY;


    private ForkConstructLlabMatrix(ImageProcessor processor, BufferedImage src, double[][] dst, int rows, int cols, int startX, int endX, int startY, int endY) {
        this.processor = processor;
        this.src = src;
        this.dst = dst;
        this.rows = rows;
        this.cols = cols;
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
    }

    public ForkConstructLlabMatrix(ImageProcessor processor, BufferedImage src) {
        this.threadCount = Runtime.getRuntime().availableProcessors();
        this.processor = processor;
        this.src = src;
        this.rows = src.getHeight();
        this.cols = src.getWidth();
        this.dst = new double[rows][cols];

    }

    @Override
    protected double[][] compute() {
        //if called for the first time, sections not yet defined, need to split task
        if (endX == 0) {
            ForkJoinTask.invokeAll(createSubtasks());
        } else {
            return process();
        }
        return null;
    }

    private RecursiveTask[] createSubtasks() {
        //initialize empty matrix
        this.dst = new double[rows][cols];

        //divide and conquer
        ForkConstructLlabMatrix[] tasks = new ForkConstructLlabMatrix[threadCount];
        //height divisible by threadCount -> divide vertically into n(=threadCount) sections
        if (rows % threadCount == 0) {
            startX = 0;
            endX = cols;
            int sectionHeight = rows / threadCount;

            for (int i = 0; i < threadCount; i++) {
                startY = i * sectionHeight;
                endY = (i + 1) * sectionHeight;
                tasks[i] = new ForkConstructLlabMatrix(processor, src, dst, rows, cols, startX, endX, startY, endY);
            }
        } else if (cols % threadCount == 0) {
            startY = 0;
            endY = rows;
            int sectionWidth = cols / threadCount;

            for (int i = 0; i < threadCount; i++) {
                startX = i * sectionWidth;
                endX = (i + 1) * sectionWidth;
                tasks[i] = new ForkConstructLlabMatrix(processor, src, dst, rows, cols, startX, endX, startY, endY);
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
                tasks[i] = new ForkConstructLlabMatrix(processor, src, dst, rows, cols, startX, endX, startY, endY);
            }
        }
        return tasks;
    }

    private double[][] process() {
        return processor.constructLlabMatrix(src, dst, startX, endX, startY, endY);
    }
}

