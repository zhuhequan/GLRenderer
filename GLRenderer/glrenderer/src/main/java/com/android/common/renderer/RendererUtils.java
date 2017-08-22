/**
 * @version 1.0
 * @author zhuhequan
 * @date 2015-06-25
 */
package com.android.common.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.WindowManager;

import com.android.common.renderer.effect.GLRenderer;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RendererUtils {
    private static Method sMethod_screenshot;

    public static void assertTrue(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    public static float clip(float value, float min, float max) {
        if (value > max) return max;
        if (value < min) return min;
        return value;
    }

    public static int clip(int value, int min, int max) {
        if (value > max) return max;
        if (value < min) return min;
        return value;
    }

    public static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30)) throw new IllegalArgumentException();
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    public static int nextMultipleN(int value, int n) {
        return ((value + n - 1) / n) * n;
    }

    public static int nextMultipleN(float value, int n) {
        return ((int) (value + n - 1) / n) * n;
    }

    public static int prevPowerOf2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }


    public static Bitmap captureScreen(Rect sourceRect, int width, int height, int minLayer, int maxLayer) {
        try {
            if (sMethod_screenshot == null) {
                sMethod_screenshot = Class.forName("android.view.SurfaceControl").
                                                  getDeclaredMethod("screenshot",
                                                  Rect.class,
                                                  int.class,
                                                  int.class,
                                                  int.class,
                                                  int.class,
                                                  boolean.class,
                                                  int.class);
            }
            return (Bitmap) sMethod_screenshot.invoke(
                              null,
                              sourceRect,
                              width,
                              height,
                              minLayer,
                              maxLayer,
                              false,
                              0);
        } catch (Exception e) {
            Log.e(GLRenderer.TAG, "captureScreen error : " + e.getMessage());
        }
        return null;
    }

    public static void blurBitmap(Bitmap srcBitmap, Bitmap dstBitmap, int radius) {
        if (srcBitmap == null || srcBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            return;
        }
        if (dstBitmap == null || dstBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            return;
        }

        Canvas canvas = new Canvas(dstBitmap);
        canvas.scale((float) dstBitmap.getWidth() / srcBitmap.getWidth(),
                (float) dstBitmap.getHeight() / srcBitmap.getHeight());
        canvas.drawBitmap(srcBitmap, 0, 0, null);
        canvas.setBitmap(null);
        radius = Math.min(radius, Math.min(dstBitmap.getWidth(), dstBitmap.getHeight()));
        GLRenderer.loadLibraryIfNeeded();
        native_blurBitmap(dstBitmap, radius);
    }

    public static String Str2MD5(String str) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    public static int windowTypeToLayerLw(int type) {
        if (type >= WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW &&
                type <= WindowManager.LayoutParams.LAST_APPLICATION_WINDOW) {
            return 2;
        }
        switch (type) {
            //WindowManager.LayoutParams.TYPE_UNIVERSE_BACKGROUND:
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+25):
                return 1;

            //WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION:
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+30):
                return 2;

            //WindowManager.LayoutParams.TYPE_WALLPAPER:
            case WindowManager.LayoutParams.TYPE_WALLPAPER:
                // wallpaper is at the bottom, though the window manager may move it.
                return 2;

            //WindowManager.LayoutParams.TYPE_PHONE
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+2):
                return 3;

            //WindowManager.LayoutParams.TYPE_SEARCH_BAR
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+1):
                return 4;

            //WindowManager.LayoutParams.TYPE_VOICE_INTERACTION:
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+31):
                // voice interaction layer is almost immediately above apps.
                return 5;

            //WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+8):
                return 6;

            //WindowManager.LayoutParams.TYPE_TOAST
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+5):
                // toasts and the plugged-in battery thing
                return 7;

            //WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+7):
                // SIM errors and unlock.  Not sure if this really should be in a high layer.
                return 8;

            //WindowManager.LayoutParams.TYPE_DREAM
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+23):
                // used for Dreams (screensavers with TYPE_DREAM windows)
                return 9;

            //WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+3):
                // like the ANR / app crashed dialogs
                return 10;

            //WindowManager.LayoutParams.TYPE_INPUT_METHOD
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+11):
                // on-screen keyboards and other such input method user interfaces go here.
                return 11;

            //WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+12):
                // on-screen keyboards and other such input method user interfaces go here.
                return 12;

            //WindowManager.LayoutParams.TYPE_KEYGUARD_SCRIM
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+29):
                // the safety window that shows behind keyguard while keyguard is starting
                return 13;

            //WindowManager.LayoutParams.TYPE_STATUS_BAR_SUB_PANEL
            case (WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW+17):
                return 14;

            //WindowManager.LayoutParams.TYPE_STATUS_BAR
            case WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW:
                return 15;
        }
        return -1;
    }

    public static int getLayer(int type){
        return windowTypeToLayerLw(type)*10000+1000;
    }
    @GLRendererNotProguard
    private static native void native_blurBitmap(Object bitmap, int r);
}

