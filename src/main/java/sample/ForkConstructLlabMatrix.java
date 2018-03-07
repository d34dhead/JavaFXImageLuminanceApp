package sample;

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class ForkConstructLlabMatrix extends RecursiveAction {

    private ImageProcessor processor;
    private BufferedImage src;
    private double[][] dst;
    int rows;
    int cols;

    public ForkConstructLlabMatrix(ImageProcessor processor, BufferedImage src, double[][] dst, int startRow, int endRow) {
        this.processor = processor;
        this.src = src;
        this.dst = dst;
        this.rows = src.getHeight();
        this.cols = src.getWidth();
    }

    @Override
    protected void compute() {
        int threads = Runtime.getRuntime().availableProcessors();
        ForkJoinTask[] tasks = new ForkJoinTask[Runtime.getRuntime().availableProcessors()];
        int sectionHeight = rows / threads;
        for (int i = 0; i < threads; i++) {
            int startRow;
            int endRow;
            //section image vertically
            if(rows%2 == 0){
                startRow = i * sectionHeight;
                endRow =(startRow + sectionHeight - 1);
            }else{
                //TODO:section an image of odd height
            }


            //tasks[i] = new ForkConstructLlabMatrix(processor, src, dst, startRow, endRow);

        }
        invokeAll(tasks);

    }
}

