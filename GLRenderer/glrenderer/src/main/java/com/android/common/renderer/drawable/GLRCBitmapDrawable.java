/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.drawable;

import android.graphics.Bitmap;

import com.android.common.renderer.functor.DrawRCBitmapFunctor;

public class GLRCBitmapDrawable extends GLBitmapDrawable {
    public GLRCBitmapDrawable(Bitmap bitmap) {
        this(new RCBitmapState(bitmap));
    }

    protected GLRCBitmapDrawable(RCBitmapState state) {
        super(state);
    }

    public void setRadius(float radius) {
        if (state().functor().getRadius() != radius) {
            state().functor().setRadius(radius);
            invalidateSelf();
        }
    }


    @Override
    protected RCBitmapState state() {
        return (RCBitmapState)mState;
    }

    public static class RCBitmapState extends BitmapState {
        RCBitmapState(Bitmap bitmap) {
            super(bitmap);
        }

        RCBitmapState(RCBitmapState state) {
            super(state);
        }

        @Override
        protected void newGLFunctor(Bitmap bitmap) {
            mDrawGLFunctor = new DrawRCBitmapFunctor();
            functor().setBitmap(bitmap);
        }

        @Override
        protected DrawRCBitmapFunctor functor() {
            return (DrawRCBitmapFunctor)mDrawGLFunctor;
        }

        @Override
        public GLRCBitmapDrawable newDrawable() {
            return new GLRCBitmapDrawable(new RCBitmapState(this));
        }
    }
}
