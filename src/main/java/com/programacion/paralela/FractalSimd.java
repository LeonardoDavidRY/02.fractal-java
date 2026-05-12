package com.programacion.paralela;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FractalSimd {
    ByteBuffer pixelBuffer;

    public FractalSimd(ByteBuffer pixelBuffer) {
        // IMPORTANTE: nativeOrder() (LITTLE_ENDIAN en x86) para que los uint32_t
        // escritos por el DLL de C++ se lean correctamente como RGBA en OpenGL.
        // Sin esto, Java interpreta los bytes en BIG_ENDIAN y los canales quedan
        // invertidos (ej: verde rgb(8,94,0) aparece como rosa/magenta).
        this.pixelBuffer = ByteBuffer.allocateDirect(FractalParams.width * FractalParams.height * 4)
                .order(ByteOrder.nativeOrder());
    }

    public void juliaSimd() {
        FractalDll.INSTANCE.julia_simd(
                FractalParams.x_Min, FractalParams.x_Max,
                FractalParams.y_Min, FractalParams.y_max,
                FractalParams.width, FractalParams.height,
                FractalParams.maxIteraciones, pixelBuffer);
    }
}
