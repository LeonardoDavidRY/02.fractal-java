package com.programacion.paralela;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;


public class FractalMain {


    private long window;
    private int textureID;
    private IntBuffer pixelBuffer;

    FractalCpu fractalCpu;
    FractalSimd fractalSimd;

    FPSCounter fpsCounter;

    int modo =  1; // 1: Cpu, 2: Simd, 3: Threaded

    public FractalMain() {
        fractalCpu = new FractalCpu();
        ByteBuffer simdBuffer = BufferUtils.createByteBuffer(FractalParams.width * FractalParams.height * 4);
        fractalSimd = new FractalSimd(simdBuffer);
        fpsCounter = new FPSCounter();
        pixelBuffer = BufferUtils.createIntBuffer(FractalParams.width * FractalParams.height);
    }

    public void run() {
        System.out.println("Fractal Julia " + Version.getVersion());
        init();
        loop();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(FractalParams.width, FractalParams.height, "Julia Set", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);

            if (key == GLFW_KEY_UP && action == GLFW_RELEASE) {
                FractalParams.maxIteraciones += 10;
            }

            if (key == GLFW_KEY_DOWN && action == GLFW_RELEASE) {
                FractalParams.maxIteraciones -= 10;
                if (FractalParams.maxIteraciones < 1) FractalParams.maxIteraciones = 0;
            }

            if (key == GLFW_KEY_1 && action == GLFW_RELEASE) {
                System.out.println("Modo java CPU");
                modo = 1;
            }

            if (key == GLFW_KEY_2 && action == GLFW_RELEASE) {
                System.out.println("Modo C/C++ SIMD");
                modo = 2;
            }

            if (key == GLFW_KEY_3 && action == GLFW_RELEASE) {
                System.out.println("Modo Java CPU Threaded (" + FractalParams.threadCount + " threads - todos los cores)");
                System.out.println("Modo Java CPU Threaded");
                modo = 3;
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window,
                (vidmode.width() - FractalParams.width) / 2,
                (vidmode.height() - FractalParams.height) / 2);
        glfwMakeContextCurrent(window);

        GL.createCapabilities();
        GL.createCapabilitiesWGL();

        //--version OpenGl
        String version = glGetString(GL_VERSION);
        String vendor = glGetString(GL_VENDOR);
        String renderer = glGetString(GL_RENDERER);

        System.out.println("OpenGl version: " + version);
        System.out.println("OpenGl vendor: " + vendor);
        System.out.println("OpenGl renderer: " + renderer);

        //--conf. proyeccion
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-1, 1, -1, 1, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_TEXTURE_2D);
        glLoadIdentity();

        glfwSwapInterval(0);
        glfwShowWindow(window);
        setupTexture();
    }

    private void setupTexture() {
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
                FractalParams.width, FractalParams.height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, NULL);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    private void loop() {
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            paint();

            int currentFps = fpsCounter.update();
            if (currentFps > 0) {
                System.out.println("FPS: " + currentFps);
            }

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }

    private void paint() {
        pixelBuffer.clear();

        if (modo == 1) {
            fractalCpu.julia_serial2(FractalParams.x_Min, FractalParams.x_Max, FractalParams.y_Min, FractalParams.y_max, FractalParams.width, FractalParams.height);
            pixelBuffer.put(fractalCpu.pixelBuffer);
        } else if (modo == 2) {
            fractalSimd.juliaSimd();
            pixelBuffer.put(fractalSimd.pixelBuffer.asIntBuffer());
        } else if (modo == 3) {
            fractalCpu.julia_threaded(FractalParams.x_Min, FractalParams.x_Max, FractalParams.y_Min, FractalParams.y_max, FractalParams.width, FractalParams.height);
            pixelBuffer.put(fractalCpu.pixelBuffer);
        }

        pixelBuffer.flip();

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, FractalParams.width, FractalParams.height, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer);
        glBegin(GL_QUADS);

        {
            glTexCoord2d(0, 0);
            glVertex2d(-1, -1);

            glTexCoord2d(0, 1);
            glVertex2d(-1, 1);

            glTexCoord2d(1, 1);
            glVertex2d(1, 1);

            glTexCoord2d(1, 0);
            glVertex2d(1, -1);
        }
        glEnd();
    }

    //JNT
    public native void julia_simd(double xml, double ymin);

    public static void main(String[] args) {
        new FractalMain().run();

    }
}