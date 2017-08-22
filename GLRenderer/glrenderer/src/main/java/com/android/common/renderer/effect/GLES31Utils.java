
package com.android.common.renderer.effect;

import android.util.Log;

import java.lang.reflect.Method;


public class GLES31Utils {
    public static final int GL_RGBA16F = 0x881A;
    public static final int GL_RGBA32F = 0x8814;
    public static final int GL_COMPUTE_SHADER = 0x91B9;
    public static final int GL_SHADER_IMAGE_ACCESS_BARRIER_BIT = 0x00000020;
    public static final int GL_TEXTURE_UPDATE_BARRIER_BIT = 0x00000100;
    public static final int GL_READ_ONLY = 0x88B8;
    public static final int GL_WRITE_ONLY = 0x88B9;

    public static Method sMethod_glBindImageTexture;
    public static Method sMethod_glDispatchCompute;
    public static Method sMethod_glMemoryBarrier;
    public static Method sMethod_glTexStorage2D;

    static {
        try {
            Class<?> clazz = Class.forName("android.opengl.GLES31");
            sMethod_glBindImageTexture = clazz.getMethod("glBindImageTexture",
                    int.class,
                    int.class,
                    int.class,
                    boolean.class,
                    int.class,
                    int.class,
                    int.class);

            sMethod_glDispatchCompute = clazz.getMethod("glDispatchCompute",
                    int.class,
                    int.class,
                    int.class);

            sMethod_glMemoryBarrier = clazz.getMethod("glMemoryBarrier",
                    int.class);

            sMethod_glTexStorage2D = clazz.getMethod("glTexStorage2D",
                    int.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class);
        } catch (Exception e) {
            Log.e(GLRenderer.TAG, "Exception: "+e.getMessage());
        }
    }

    public static void glBindImageTexture(
            int unit,
            int texture,
            int level,
            boolean layered,
            int layer,
            int access,
            int format) {
        try {
            sMethod_glBindImageTexture.invoke(null, unit, texture, level, layered, layer, access, format);
        } catch (Exception e) {
            Log.e(GLRenderer.TAG, "Exception glBindImageTexture: "+e.getMessage());
        }
    }

    public static void glDispatchCompute(
            int num_groups_x,
            int num_groups_y,
            int num_groups_z) {
        try {
            sMethod_glDispatchCompute.invoke(null, num_groups_x, num_groups_y, num_groups_z);
        } catch (Exception e) {
            Log.e(GLRenderer.TAG, "Exception glDispatchCompute: "+e.getMessage());
        }
    }

    public static void glMemoryBarrier(int barriers) {
        try {
            sMethod_glMemoryBarrier.invoke(null, barriers);
        } catch (Exception e) {
            Log.e(GLRenderer.TAG, "Exception glMemoryBarrier: "+e.getMessage());
        }
    }

    public static void glTexStorage2D(
            int target,
            int levels,
            int internalformat,
            int width,
            int height) {
        try {
            sMethod_glTexStorage2D.invoke(null, target, levels, internalformat, width, height);
        } catch (Exception e) {
            Log.e(GLRenderer.TAG, "Exception glTexStorage2D: "+e.getMessage());
        }
    }
}
