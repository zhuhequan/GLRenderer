package com.android.common.renderer.drawable;
/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
import com.android.common.renderer.functor.DrawGLRendererFunctor;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRendererDrawable extends GLDrawable {

    public GLRendererDrawable(Renderer renderer) {
        this(new GLRendererState(renderer));
    }

    protected GLRendererDrawable(GLRendererState state) {
        super(state);
    }

    public void setTrimLevel(int level) {
        state().functor().setTrimLevel(level);
    }

    @Override
    protected GLRendererState state() {
        return (GLRendererState)mState;
    }

    public static class GLRendererState extends GLState {
        GLRendererState(Renderer renderer) {
            mDrawGLFunctor = new DrawGLRendererFunctor(renderer);
        }

        @Override
        public GLRendererDrawable newDrawable() {
            return null;
        }

        @Override
        protected DrawGLRendererFunctor functor() {
            return (DrawGLRendererFunctor)mDrawGLFunctor;
        }
    }

    public interface Renderer {
        public void onSurfaceCreated(GL10 gl, EGLConfig config);
        public void onSurfaceReleased(boolean hasEglContext);
        public void onSurfaceChanged(GL10 gl, int width, int height);
        public void onDrawFrame(GL10 gl);
    }
}
