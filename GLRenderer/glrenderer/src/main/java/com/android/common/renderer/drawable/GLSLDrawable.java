package com.android.common.renderer.drawable;
/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
import com.android.common.renderer.RendererUtils;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.functor.DrawGLSLFunctor;

public class GLSLDrawable extends GLDrawable {

    public GLSLDrawable(String glsl) {
        this(new GLSLState(glsl));
    }

    public GLSLDrawable(int resId) {
        this(new GLSLState(resId));
    }

    protected GLSLDrawable(GLSLState state) {
        super(state);
    }


    public void setTime(float time) {
        state().functor().setTime(time);
    }

    /**
     * 3个通用参数，供外部传值到shader里面使用，如果shader里面需要使用额外的参数，
     * 可以使用这3个参数传入。
     * 每次ondraw的时候都会读取这些值。
     * @param value
     */
    public void setParameter1(float value) {
        state().functor().setParameter1(value);
    }
    public void setParameter2(float value) {
        state().functor().setParameter2(value);
    }
    public void setParameter3(float value) {
        state().functor().setParameter3(value);
    }

    public float getTime() {
        return state().functor().getTime();
    }

    public float getParameter1() {
        return state().functor().getParameter1();
    }
    public float getParameter2() {
        return state().functor().getParameter2();
    }
    public float getParameter3() {
        return state().functor().getParameter3();
    }

    @Override
    protected GLSLState state() {
        return (GLSLState)mState;
    }

    public static class GLSLState extends GLState {
        GLSLState(String glsl) {
            mDrawGLFunctor = new DrawGLSLFunctor(RendererUtils.Str2MD5(glsl), glsl);
        }

        GLSLState(int resId) {
            this(GLRenderer.getResources().getString(resId));
        }

        GLSLState(GLSLState state) {
            mDrawGLFunctor = new DrawGLSLFunctor(functor().getEffect(), functor().getGLSL());
            mDrawGLFunctor.setAlpha(state.mDrawGLFunctor.getAlpha());
            mChangingConfigurations = state.mChangingConfigurations;
        }

        @Override
        public GLSLDrawable newDrawable() {
            return new GLSLDrawable(new GLSLState(this));
        }

        @Override
        protected DrawGLSLFunctor functor() {
            return (DrawGLSLFunctor)mDrawGLFunctor;
        }
    }
}
