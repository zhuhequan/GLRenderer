/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.drawable;

import android.graphics.PorterDuff.Mode;

import com.android.common.renderer.effect.render.AbstractBlurRender.Parameters;
import com.android.common.renderer.functor.AbstractBlurFunctor;

abstract class GLAbstractBlurDrawable extends GLDrawable {
    protected GLAbstractBlurDrawable(BaseBlurState state) {
        super(state);
    }

    public void setBlurLevel(float level) {
        level = (float)(Math.round(level*100))/100;
        if (parameter().getLevel() != level) {
            parameter().setLevel(level);
            invalidateSelf();
        }
    }

    public float getBlurLevel() {
        return parameter().getLevel();
    }


    public void setRadius(int radius) {
        if (parameter().getRadius() != radius) {
            parameter().setRadius(radius);
            invalidateSelf();
        }
    }

    public void setPassCount(int count) {
        count = Math.min(10, count);
        if (parameter().getPassCount() != count) {
            parameter().setPassCount(count);
            invalidateSelf();
        }
    }

    public void setScale(float scale) {
        if (parameter().getScale() != scale) {
            parameter().setScale(scale);
            invalidateSelf();
        }
    }

    public void setIntensity(float intensity) {
        if (parameter().getIntensity() != intensity) {
            parameter().setIntensity(intensity);
            invalidateSelf();
        }
    }


    public void setColorFilter(int color) {
        if (parameter().getFilterColor() != color) {
            parameter().setFilterColor(color);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(int color, Mode mode) {
        setColorFilter(color);
    }

    protected Parameters parameter() {
        return state().functor().getParameters();
    }


    abstract protected BaseBlurState state();

    abstract public static class BaseBlurState extends GLState {
        protected BaseBlurState(boolean progress) {
            newGLFunctor(progress);
        }

        protected BaseBlurState(BaseBlurState state) {
            newGLFunctor(state.functor().isProgress());
            functor().getParameters().setLevel(state.functor().getParameters().getLevel());
            functor().getParameters().setFilterColor(state.functor().getParameters().getFilterColor());
            mDrawGLFunctor.setAlpha(state.mDrawGLFunctor.getAlpha());
            mChangingConfigurations = state.mChangingConfigurations;
        }

        abstract protected void newGLFunctor(boolean progress);
        abstract protected AbstractBlurFunctor functor();
    }
}
