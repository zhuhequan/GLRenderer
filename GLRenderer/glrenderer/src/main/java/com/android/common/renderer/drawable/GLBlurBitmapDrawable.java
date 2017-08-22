/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.drawable;

import android.graphics.Bitmap;
import android.os.Build;

import com.android.common.renderer.functor.DrawBlurBitmapFunctor;

public class GLBlurBitmapDrawable extends GLAbstractBlurDrawable {

    public GLBlurBitmapDrawable(Bitmap bitmap) {
        this(new BlurBitmapState(bitmap, false));
    }

    public GLBlurBitmapDrawable(Bitmap bitmap, boolean progress) {
        this(new BlurBitmapState(bitmap, progress));
    }

    protected GLBlurBitmapDrawable(BlurBitmapState state) {
        super(state);
    }

    public void setBitmap(Bitmap bitmap) {
        ((DrawBlurBitmapFunctor)mState.mDrawGLFunctor).setBitmap(bitmap);
    }

    @Override
    protected BlurBitmapState state() {
        return (BlurBitmapState)mState;
    }

    public static class BlurBitmapState extends BaseBlurState {
        protected BlurBitmapState(Bitmap bitmap, boolean progress) {
            super(progress);
            functor().setBitmap(bitmap);
        }

        protected BlurBitmapState(BlurBitmapState state) {
            super(state);
            functor().setBitmap(state.functor().getBitmap());
        }

        @Override
        protected void newGLFunctor(boolean progress) {
            progress &= (Build.VERSION.SDK_INT >= 21);
            mDrawGLFunctor = new DrawBlurBitmapFunctor(progress);
        }

        @Override
        protected DrawBlurBitmapFunctor functor() {
            return (DrawBlurBitmapFunctor)mDrawGLFunctor;
        }

        @Override
        public GLBlurBitmapDrawable newDrawable() {
            return new GLBlurBitmapDrawable(new BlurBitmapState(this));
        }
    }
}
