package com.programacion.paralela;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;


public class FractalMain {


    // The window handle
    private long window;
    private int textureID;

    FractalCpu fractalCpu = new FractalCpu();
    FPSCounter fpsCounter = new FPSCounter();

    public FractalMain() {
        fractalCpu = new FractalCpu();
        fpsCounter = new FPSCounter();
    }

    public void run() {
        System.out.println("Fractal Julia " + Version.getVersion());

        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(FractalParams.WIDTH, FractalParams.HEIGHT, "Julia Set", NULL, NULL);
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
            }
            if (FractalParams.maxIteraciones < 0) FractalParams.maxIteraciones = 10;
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - FractalParams.WIDTH) / 2,
                (vidmode.height() - FractalParams.HEIGHT) / 2);

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GL.createCapabilities();

        String version = GL11.glGetString(GL11.GL_VERSION);
        String vendor = GL11.glGetString(GL11.GL_VENDOR);
        String render = GL11.glGetString(GL11.GL_RENDERER);

        System.out.println("OpenGL Version: " + version);
        System.out.println("OpenGL Vendor: " + vendor);
        System.out.println("OpenGL Renderer: " + render);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-1, 1, -1, 1, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glEnable(GL11.GL_TEXTURE_2D);
        glLoadIdentity();


        glfwSwapInterval(1);
        glfwShowWindow(window);

        setupTexture();
    }

    private void setupTexture() {
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexImage2D(
                GL_TEXTURE_2D, 0, GL_RGBA8,
                FractalParams.WIDTH, FractalParams.HEIGHT, 0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                NULL
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    }

    private void loop() {

        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);


        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            paint();

            glfwSwapBuffers(window);

            glfwPollEvents();


        }

    }

    private void paint() {
        int currentFps = fpsCounter.update();
        System.out.println("FPS: " + currentFps);
        fractalCpu.julia_serial2(FractalParams.xMin, FractalParams.xMax, FractalParams.yMin, FractalParams.yMax, FractalParams.WIDTH, FractalParams.HEIGHT);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8,
                FractalParams.WIDTH, FractalParams.HEIGHT, 0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                fractalCpu.pixel_buffer);

        glBegin(GL_QUADS);
        {
            glTexCoord2d(0,0);
            glVertex2d(-1, -1);

            glTexCoord2d(0,1);
            glVertex2d(-1, 1);

            glTexCoord2d(1,1);
            glVertex2d(1, 1);

            glTexCoord2d(1,0);
            glVertex2d(1, -1);

        }
        glEnd();
    }

    public static void main(String[] args) {
        new FractalMain().run();

    }
}