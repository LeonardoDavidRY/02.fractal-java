package com.programacion.paralela;

public class FractalParams {
    public static final int width = 1600;
    public static final int height = 900;

    // Core detection for threading
    public static final int availableCores = Runtime.getRuntime().availableProcessors();
    public static final int threadCount = Math.max(1, availableCores);

    public static final double x_Min = -1.5;
    public static final double x_Max = 1.5;
    public static final double y_Min = -1.0;
    public static final double y_max = 1.0;

    public static int maxIteraciones = 10;
    public static final double cReal = -0.7;
    public static final double cImag = 0.27015;
    public static final int PALETTE_SIZE = 16;

    // Java mode: red-black palette
    public static final int[] javaColorRamp = {
            0xFFFF0000,  // #FF0000
            0xFFF20000,  // #F20000
            0xFFE50000,  // #E50000
            0xFFD80000,  // #D80000
            0xFFCC0000,  // #CC0000
            0xFFBF0000,  // #BF0000
            0xFFB20000,  // #B20000
            0xFFA50000,  // #A50000
            0xFF990000,  // #990000
            0xFF8C0000,  // #8C0000
            0xFF7F0000,  // #7F0000
            0xFF720000,  // #720000
            0xFF660000,  // #660000
            0xFF590000,  // #590000
            0xFF4C0000,  // #4C0000
            0xFF3F0000,  // #3F0000
            0xFF330000,  // #330000
            0xFF260000,  // #260000
            0xFF190000,  // #190000
            0xFF0C0000,  // #0C0000
            0xFF000000   // #000000
    };

    public static final int[] cppColorRamp = {
            0xFF085E00,  // Verde oscuro
            0xFF075900,
            0xFF075400,
            0xFF064F00,
            0xFF064B00,
            0xFF064600,
            0xFF054100,
            0xFF053D00,
            0xFF043800,
            0xFF043300,
            0xFF042F00,
            0xFF032A00,
            0xFF032500,
            0xFF022000,
            0xFF021C00,
            0xFF021700,
            0xFF011200,
            0xFF010E00,
            0xFF000900,
            0xFF000400,
            0xFF000000   // Negro
    };

    // Threaded mode: blue-purple gradient palette
    public static final int[] threadedColorRamp = {
            0xFFA168CC,
            0xFF9F70CD,
            0xFF9D78CF,
            0xFF9B80D0,
            0xFF9988D1,
            0xFF9790D3,
            0xFF9598D4,
            0xFF93A0D5,
            0xFF91A8D7,
            0xFF8EB0D8,
            0xFF8CB8D9,
            0xFF8AC0DA,
            0xFF88C8DC,
            0xFF86D0DD,
            0xFF84D8DE,
            0xFF82E0E0,
            0xFF80E8E1
    };


    // Legacy colorRamp for backward compatibility
    public static final int[] colorRamp = javaColorRamp;

}
