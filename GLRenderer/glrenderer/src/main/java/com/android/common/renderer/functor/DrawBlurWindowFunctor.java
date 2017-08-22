/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.functor;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.android.common.renderer.effect.GLRenderer;
 public class DrawBlurWindowFunctor extends DrawBlurBitmapFunctor {
    private static final float SCALE = 0.067f;
    protected int mOrientation;

    public static DisplayMetrics METRICS;
    private static Display DISPLAY;

    static {
        WindowManager wm  = (WindowManager)
                GLRenderer.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        DISPLAY = wm.getDefaultDisplay();
        METRICS = new DisplayMetrics();
        DISPLAY.getMetrics(METRICS);
    }

    public DrawBlurWindowFunctor(boolean progress) {
        super(progress);
        if (!progress) {
            mParameters.setScale(SCALE);
        }
    }

     @Override
     public void setBitmap(Bitmap bitmap) {
         super.setBitmap(bitmap);
         mOrientation = getDisplayRotation();
     }

     @Override
     protected int getOrientation() {
         return mOrientation;
     }

     @Override
     protected boolean useOrigTexture() {
         return true;
     }

     protected int getDisplayRotation() {
        int value = DISPLAY.getRotation();
        switch (value) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

}
