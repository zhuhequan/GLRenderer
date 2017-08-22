/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.functor.DrawGLFunctor;

abstract public class GLDrawable extends Drawable {
    protected GLState mState;
    protected GLDrawable(GLState state) {
        mState = state;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            mState.functor().draw(canvas);
        } else {
            mState.functor().draw(canvas, bounds.left, bounds.top, bounds.right, bounds.bottom);
        }
    }

    public void invalidate() {
        mState.functor().invalidate();
    }

    public void setAlpha(int alpha) {
        if (getAlpha() != alpha) {
            mState.functor().setAlpha(alpha);
            invalidateSelf();
        }
    }

    public int getAlpha() {
        return mState.functor().getAlpha();
    }

    public void recycle() {
        mState.functor().trimResources(GLRenderer.TRIM_MEMORY_UI_HIDDEN, false);
    }


    @Override
    public int getOpacity() {
        return getAlpha() == 0xff ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (changed && !visible) {
            recycle();
        }
        return changed;
    }

    @Override
    public ConstantState getConstantState() {
        mState.mChangingConfigurations = getChangingConfigurations();
        return mState;
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mState.mChangingConfigurations;
    }


    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    protected abstract GLState state();

    abstract public static class GLState extends ConstantState {
        DrawGLFunctor mDrawGLFunctor;
        int mChangingConfigurations;

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
        abstract protected DrawGLFunctor functor();
    }
}
