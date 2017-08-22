/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */

package com.android.common.renderer.drawable;

import android.graphics.Bitmap;

import com.android.common.renderer.functor.DrawBitmapFunctor;

public class GLBitmapDrawable extends GLDrawable {

    public GLBitmapDrawable(Bitmap bitmap) {
        this(new BitmapState(bitmap));
    }

    protected GLBitmapDrawable(BitmapState state) {
        super(state);
    }

    @Override
    public int getIntrinsicWidth() {
        return state().functor().getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return state().functor().getHeight();
    }

    public void setEffect(String effect) {
        if (effect == null) return;
        if (!effect.equals(getEffect())) {
            mState.mDrawGLFunctor.setEffect(effect);
            invalidateSelf();
        }
    }

    public String getEffect() {
        return mState.mDrawGLFunctor.getEffect();
    }

    @Override
    protected BitmapState state() {
        return (BitmapState)mState;
    }

    public static class BitmapState extends GLState {
        BitmapState(Bitmap bitmap) {
            newGLFunctor(bitmap);
        }

        BitmapState(BitmapState state) {
            newGLFunctor(functor().getBitmap());
            mDrawGLFunctor.setEffect(state.mDrawGLFunctor.getEffect());
            mDrawGLFunctor.setAlpha(state.mDrawGLFunctor.getAlpha());
            mChangingConfigurations = state.mChangingConfigurations;
        }

        protected void newGLFunctor(Bitmap bitmap) {
            mDrawGLFunctor = new DrawBitmapFunctor();
            functor().setBitmap(bitmap);
        }

        @Override
        public GLBitmapDrawable newDrawable() {
            return new GLBitmapDrawable(new BitmapState(this));
        }

        @Override
        protected DrawBitmapFunctor functor() {
            return (DrawBitmapFunctor)mDrawGLFunctor;
        }
    }
}
