package core;

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ConstructLlabMatrixForkTask extends RecursiveTask<double[][]> {

    private static ImageProcessor processor;
    private static BufferedImage src;
    private static double[][] dst;
    private static int rows;
    private static int cols;
    private static int threadCount;
    private int startX;
    private int endX;
    private int startY;
    private int endY;


    private ConstructLlabMatrixForkTask(int startX, int endX, int startY, int endY) {
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
    }

    public ConstructLlabMatrixForkTask(ImageProcessor processor, BufferedImage src) {
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
            return dst;
        } else {
            return process();
        }
    }

    private RecursiveTask[] createSubtasks() {
        //divide and conquer
        ConstructLlabMatrixForkTask[] tasks =
                new ConstructLlabMatrixForkTask[threadCount];
        //height divisible by threadCount -> divide vertically into n(=threadCount) sections
        if (rows % threadCount == 0) {
            startX = 0;
            endX = cols;
            int sectionHeight = rows / threadCount;

            for (int i = 0; i < threadCount; i++) {
                startY = i * sectionHeight;
                endY = (i + 1) * sectionHeight;
                tasks[i] = new ConstructLlabMatrixForkTask(startX, endX, startY, endY);
            }
        } else if (cols % threadCount == 0) {
            startY = 0;
            endY = rows;
            int sectionWidth = cols / threadCount;

            for (int i = 0; i < threadCount; i++) {
                startX = i * sectionWidth;
                endX = (i + 1) * sectionWidth;
                tasks[i] = new ConstructLlabMatrixForkTask(startX, endX, startY, endY);
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
                tasks[i] = new ConstructLlabMatrixForkTask(startX, endX, startY, endY);
            }
        }
        return tasks;
    }

    private double[][] process() {
        return processor.constructLlabMatrix(src, dst, startX, endX, startY, endY);
    }
}

