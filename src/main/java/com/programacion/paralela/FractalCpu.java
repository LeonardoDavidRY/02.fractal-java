package com.programacion.paralela;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FractalCpu {
    public int pixelBuffer[];

    public FractalCpu() {
        pixelBuffer = new int[FractalParams.width * FractalParams.height];
    }

    int acotado_2(double x, double y, int[] colorPalette)
    {
    /*
    dados: c,zo
    Zn+1 = Zn^2 + c
    */
        int iter = 1;

        double zr = x;
        double zi = y;

        while (iter < FractalParams.maxIteraciones && zr * zr + zi * zi <= 4.0)
        {
            double dr = zr*zr - zi*zi + FractalParams.cReal;
            double di = 2.0*zr*zi + FractalParams.cImag;
            zr = dr;
            zi = di;

            iter++;
        }
        if (iter < FractalParams.maxIteraciones)
        {
            int index = iter % colorPalette.length;
            return colorPalette[index];
        }
        return 0xFF000000; // Negro
    }

    int acotado_2(double x, double y)
    {
        return acotado_2(x, y, FractalParams.javaColorRamp);
    }

    void julia_serial2(double x_min, double x_max, double y_min, double y_max, int width, int height)
    {
        double dx = (x_max - x_min) / width;
        double dy = (y_max - y_min) / height;
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                // z = x+yi = (x,y)
                double x = x_min + i * dx;
                double y = y_max - j * dy;

                var color = acotado_2(x, y);
                pixelBuffer[i + j * width] = color;
            }
        }
    }

    void julia_threaded(double x_min, double x_max, double y_min, double y_max, int width, int height)
    {
        System.out.println("Usando " + FractalParams.threadCount + " threads (todos los cores) con paleta azul-magenta");

        ExecutorService executor = Executors.newFixedThreadPool(FractalParams.threadCount);

        double dx = (x_max - x_min) / width;
        double dy = (y_max - y_min) / height;

        // Divide work among threads by rows
        int rowsPerThread = height / FractalParams.threadCount;
        int remainingRows = height % FractalParams.threadCount;

        for (int threadId = 0; threadId < FractalParams.threadCount; threadId++) {
            final int startRow = threadId * rowsPerThread;
            final int endRow = (threadId == FractalParams.threadCount - 1) ?
                    startRow + rowsPerThread + remainingRows : startRow + rowsPerThread;
            final int currentThreadId = threadId;

            executor.submit(() -> {
                for (int j = startRow; j < endRow; j++) {
                    for (int i = 0; i < width; i++) {
                        double x = x_min + i * dx;
                        double y = y_max - j * dy;

                        int color = acotado_2(x, y, FractalParams.threadedColorRamp);
                        pixelBuffer[i + j * width] = color;
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread execution interrupted");
        }
    }
}
