/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.drawable;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;

import com.android.common.renderer.RendererUtils;
import com.android.common.renderer.functor.DrawBlurWindowFunctor;

public class GLBlurWindowDrawable extends GLBlurBitmapDrawable {

    private Bitmap mDefaultBimtap;
    public GLBlurWindowDrawable() {
        this(new BlurWindowState(false));
    }


    public GLBlurWindowDrawable(boolean progress) {
       super(null, progress);
    }

    protected GLBlurWindowDrawable(BlurWindowState state) {
        super(state);
    }

    public void captureScreen() {
        captureScreen(0, -1);
    }

    public void captureScreen(int minLayer, int maxLayer) {
        int width = DrawBlurWindowFunctor.METRICS.widthPixels/2;
        int height = DrawBlurWindowFunctor.METRICS.heightPixels/2;
        captureScreen(width, height, minLayer, maxLayer);
    }

    public void setDefaultBimtap(Bitmap bitmap) {
        mDefaultBimtap = bitmap;
    }

    public void captureScreen(int width, int height, int minLayer, int maxLayer) {
        captureScreen(new Rect(), width, height, minLayer, maxLayer);
    }

    public void captureScreen(Rect sourceRect, int minLayer, int maxLayer) {
        captureScreen(sourceRect, sourceRect.width(),sourceRect.height(),minLayer,maxLayer);
    }

    public void captureScreen(Rect sourceRect, int width, int height, int minLayer, int maxLayer) {
        //因为截屏返回的bitmap内部pixels是对应一个GraphicBuffer,如果宽不是32的倍数bitmap的width与stride会不一样
        //在部分GPU上width和stride不一样会有兼容性问题,具体表现为上传的纹理像素篡位
        width = (width+31)&(~31);
        Bitmap bitmap = RendererUtils.captureScreen(sourceRect, width, height, minLayer, maxLayer);
        if (bitmap == null) {
            if (mDefaultBimtap == null) {
                mDefaultBimtap = Bitmap.createBitmap(9,16, Bitmap.Config.ARGB_8888);
                mDefaultBimtap.eraseColor(0xff000000);
            }
            bitmap = mDefaultBimtap;
        }
        setBitmap(bitmap);
    }


    public static class BlurWindowState extends BlurBitmapState {
        BlurWindowState(boolean progress) {
            super(null, progress);
        }

        BlurWindowState(BlurWindowState state) {
            super(state);
        }

        @Override
        public GLBlurWindowDrawable newDrawable() {
            return new GLBlurWindowDrawable(new BlurWindowState(this));
        }

        @Override
        protected DrawBlurWindowFunctor functor() {
            return (DrawBlurWindowFunctor)mDrawGLFunctor;
        }

        @Override
        protected void newGLFunctor(boolean progress) {
            progress &= (Build.VERSION.SDK_INT >= 21);
            mDrawGLFunctor = new DrawBlurWindowFunctor(progress);
        }
    }

}
