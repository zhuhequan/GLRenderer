/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.drawable;

import android.os.Build;

import com.android.common.renderer.functor.DrawBlurFunctor;

public class GLBlurDrawable extends GLAbstractBlurDrawable {
    public GLBlurDrawable(boolean progress) {
        this(new BlurState(progress));
    }

    public GLBlurDrawable() {
        this(1.0f);
    }

    public GLBlurDrawable(float level) {
        this(new BlurState(false));
        parameter().setLevel(level);
    }

    protected GLBlurDrawable(BlurState state) {
        super(state);
    }

    public void setRegionUpdate(boolean regionUpdate) {
        state().functor().setRegionUpdate(regionUpdate);
    }

    public void setIgnoreLayer(boolean ignore) {
        state().functor().setIgnoreLayer(ignore);
    }

    /**
     * 是否为静态，静态不会调用copytexture获取下层的截图进行模糊
     * @param isStatic
     */
    public void setStatic(boolean isStatic) {
        state().functor().setStatic(isStatic);
    }

    public boolean isStatic() {
        return state().functor().isStatic();
    }

    @Override
    protected BlurState state() {
        return (BlurState)mState;
    }

    public static class BlurState extends BaseBlurState {
        BlurState(boolean progress) {
            super(progress);
        }

        BlurState(BlurState state) {
            super(state);
        }

        @Override
        protected void newGLFunctor(boolean progress) {
            progress &= (Build.VERSION.SDK_INT >= 21);
            mDrawGLFunctor = new DrawBlurFunctor(progress);
        }

        @Override
        protected DrawBlurFunctor functor() {
            return (DrawBlurFunctor)mDrawGLFunctor;
        }

        @Override
        public GLBlurDrawable newDrawable() {
            return new GLBlurDrawable(new BlurState(this));
        }
    }
}
