/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2017-02-18
 * */
package com.android.common.renderer;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.Log;

public class EGLBitmap  {
    private static final String TAG = "EGLBitmap";
    //copy from GraphicBuffer.java
    public static final int USAGE_SW_READ_NEVER = 0x0;
    public static final int USAGE_SW_READ_RARELY = 0x2;
    public static final int USAGE_SW_READ_OFTEN = 0x3;
    public static final int USAGE_SW_READ_MASK = 0xF;

    public static final int USAGE_SW_WRITE_NEVER = 0x0;
    public static final int USAGE_SW_WRITE_RARELY = 0x20;
    public static final int USAGE_SW_WRITE_OFTEN = 0x30;
    public static final int USAGE_SW_WRITE_MASK = 0xF0;
    public static final int USAGE_PROTECTED = 0x4000;
    public static final int USAGE_HW_TEXTURE = 0x100;
    public static final int USAGE_HW_RENDER = 0x200;
    public static final int USAGE_HW_2D = 0x400;
    public static final int USAGE_HW_COMPOSER = 0x800;
    public static final int USAGE_HW_VIDEO_ENCODER = 0x10000;
    public static final int USAGE_HW_MASK = 0x71F00;
    public static final int DEFAULT_USAGE = USAGE_HW_TEXTURE | USAGE_SW_READ_OFTEN | USAGE_SW_WRITE_OFTEN;
    public static final int HAL_PIXEL_FORMAT_BGRA_8888 = 5;
    private long mNativeBitmap;
    private int mWidth;
    private int mHeight;
    private int mFormat = PixelFormat.UNKNOWN;
    private int mUsage = USAGE_HW_TEXTURE | USAGE_SW_READ_OFTEN | USAGE_SW_WRITE_OFTEN;

    public EGLBitmap(EGLBitmap bitmap) {
        if (bitmap == null || bitmap.isReleased()) {
            Log.e(TAG, "EGLBitmap is released.");
            return;
        }
        mNativeBitmap = native_dup(bitmap.mNativeBitmap);
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        mFormat = bitmap.getFormat();
        mUsage = bitmap.getUsage();
    }

    public EGLBitmap(int width, int height, int format, int usage) {
        mNativeBitmap = native_alloc(width, height, format, usage);
        mWidth = width;
        mHeight = height;
        mFormat = format;
        mUsage = usage;
    }

    public EGLBitmap(int width, int height) {
        this(width, height, PixelFormat.RGBA_8888, DEFAULT_USAGE);
    }

    public void fillBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        if (isReleased()) {
            Log.e(TAG, "EGLBitmap is released.");
            return;
        }
        if (bitmap.getConfig() != bitmapConfig(mFormat)) {
            Log.e(TAG,"Bitmap Config is invalid.");
            return;
        }
        if (bitmap.getWidth() != mWidth || bitmap.getHeight() != mHeight) {
            Log.e(TAG,"Bitmap size is invalid.");
            return;
        }
        if (!bitmap.isMutable()) {
            Log.e(TAG,"Bitmap is not mutable.");
            return;
        }
        native_fillBitmap(mNativeBitmap, bitmap);
    }

    public Bitmap getBitmap() {
        if (isReleased()) {
            Log.e(TAG, "EGLBitmap is released.");
            return null;
        }
        return (Bitmap)native_getBitmap(mNativeBitmap);
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        if (isReleased()) {
             Log.e(TAG, "EGLBitmap is released.");
             return;
        }
        if (bitmap.getConfig() != bitmapConfig(mFormat)) {
            Log.e(TAG, "Bitmap Config is invalid.");
            return;
        }
        if (bitmap.getWidth() != mWidth || bitmap.getHeight() != mHeight) {
            Log.e(TAG,"Bitmap size is invalid.");
            return;
        }
        native_setBitmap(mNativeBitmap, bitmap);
    }

    // call in render thread
    public boolean bindTexture(int texId) {
        if (isReleased()) {
            Log.e(TAG, "EGLBitmap is released.");
            return false;
        }
        return native_bindTexture(mNativeBitmap, texId);
    }

    public void release() {
        if (!isReleased()) {
            native_release(mNativeBitmap);
            mNativeBitmap = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }

    private boolean isReleased() {
        return mNativeBitmap == 0;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getFormat() {
        return mFormat;
    }

    public int getUsage() {
        return mUsage;
    }

    public boolean isEqual(EGLBitmap bitmap) {
        if (bitmap == null || bitmap.isReleased()) {
            Log.e(TAG, "EGLBitmap is invalid.");
            return false;
        }
        if (isReleased()) {
            return false;
        }
        return native_sameAs(mNativeBitmap, bitmap.mNativeBitmap);
    }
    public static int pixelFormat(Bitmap.Config format) {
        if (format == Bitmap.Config.ARGB_8888) {
            return PixelFormat.RGBA_8888;
        } else if (format == Bitmap.Config.RGB_565) {
            return PixelFormat.RGB_565;
        }
        return PixelFormat.UNKNOWN;
    }

    public static Bitmap.Config bitmapConfig(int format) {
        switch(format) {
            case PixelFormat.RGBA_8888:
            case PixelFormat.RGBX_8888:
            case HAL_PIXEL_FORMAT_BGRA_8888:
                return Bitmap.Config.ARGB_8888;
            case PixelFormat.RGB_565:
                return Bitmap.Config.RGB_565;
            default:
                return null;
        }
    }

    static {
        System.loadLibrary("eglbitmap");
    }

    private native static long native_alloc(int width, int height, int format, int usage);
    private native static void native_release(long nativeBitmap);
    private native static long native_dup(long nativeBitmap);
    private native static boolean native_sameAs(long lNativeBitmap, long rNativeBitmap);
    private native static void native_setBitmap(long nativeBitmap, Object bitmap);
    private native static Object native_getBitmap(long nativeBitmap);
    private native static void native_fillBitmap(long nativeBitmap, Object bitmap);
    private native static boolean native_bindTexture(long nativeBitmap, int texId);
}

