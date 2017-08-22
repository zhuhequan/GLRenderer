package com.android.common.renderer.functor;
/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
import android.graphics.Rect;
import android.opengl.GLES20;
import android.util.Log;

import com.android.common.renderer.drawable.GLRendererDrawable;
import com.android.common.renderer.effect.GLRenderer;

import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;


public class DrawGLRendererFunctor extends DrawGLFunctor {

    private boolean mCreated = false;
    private int mWidth = 0;
    private int mHeight = 0;

    private GLRendererDrawable.Renderer mRenderer;
    private int mTrimLevel = GLRenderer.TRIM_MEMORY_UI_HIDDEN;

    public DrawGLRendererFunctor(GLRendererDrawable.Renderer renderer) {
        mRenderer = renderer;
    }

    public void setTrimLevel(int level) {
        level = Math.min(Math.max(GLRenderer.TRIM_MEMORY_UI_HIDDEN,level),
                GLRenderer.TRIM_MEMORY_COMPLETE);
        mTrimLevel = level;
    }

    protected void onDraw(GLInfo glInfo) {
        synchronized (this) {
            EGLContext eglContext = GLRenderer.getCurrentEGLContext();
            if (eglContext == null) {
                Log.e(TAG, "No EGLContext "+this);
                return;
            }

            GL10 gl10 = (GL10)eglContext.getGL();
            if (!mCreated) {
                mCreated = true;
                mRenderer.onSurfaceCreated(gl10, null);
            }

            Rect r = mSourceBounds;

            int width = r.width();
            int height = r.height();
            int left = (int)(r.left + glInfo.transform[12]);
            int top = (int)(glInfo.viewportHeight - (r.top + height + glInfo.transform[13]));

            GLES20.glViewport(left, top, width, height);

            if (mWidth != width || mHeight != height) {
                mWidth = width;
                mHeight = height;
                mRenderer.onSurfaceChanged(gl10, mWidth, mHeight);
            }

            mRenderer.onDrawFrame(gl10);
        }
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        super.trimResources(level, hasEglContext);
        synchronized (this) {
            if (level >= mTrimLevel) {
                mCreated = false;
                mRenderer.onSurfaceReleased(hasEglContext);
            }
        }
    }
}
