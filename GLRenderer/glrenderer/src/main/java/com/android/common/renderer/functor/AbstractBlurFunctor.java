/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.functor;

import android.graphics.Rect;
import android.util.Log;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLCanvasImpl;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.op.DrawTextureOp;
import com.android.common.renderer.effect.render.AbstractBlurRender;
import com.android.common.renderer.effect.render.AbstractBlurRender.BlurResult;
import com.android.common.renderer.effect.render.AbstractBlurRender.Parameters;
import com.android.common.renderer.effect.render.ProgressBlurRender;
import com.android.common.renderer.effect.render.Render;
import com.android.common.renderer.effect.render.StaticBlurRender;

abstract public class AbstractBlurFunctor extends DrawGLFunctor {
    protected Parameters mParameters = new Parameters();
    protected Parameters mDrawingParameters = new Parameters();

    protected DrawTextureOp mDrawTextureOp = new DrawTextureOp();
    protected DrawInfo mDrawInfo = new DrawInfo();
    protected Rect mTargetBounds = new Rect();

    protected boolean mInvalidate = true;
    protected BlurResult mBlurResult;
    protected boolean mProgress = false;

    public AbstractBlurFunctor(boolean progress) {
        mEffectKey = Render.BLUR;
        mProgress = progress;
        if (progress) {
            mParameters.setScale(0.4f);
            mParameters.setPassCount(3);
            mParameters.setRadius(30);
        } else {
            mParameters.setScale(0.06f);
            mParameters.setPassCount(3);
            mParameters.setRadius(4);
        }
    }

    protected void draw(GLCanvasImpl canvas, GLInfo glInfo) {}


    public void setParameters(Parameters p) {
        mParameters.copyFrom(p);
    }

    public Parameters getParameters() {
        return mParameters;
    }

    public boolean isProgress() {
        return mProgress;
    }

    @Override
    public AbstractBlurRender getRender(GLCanvas canvas) {
        return mProgress ? ProgressBlurRender.getInstance(canvas):
                (StaticBlurRender)canvas.getRender(Render.BLUR);
    }

    @Override
    protected void onDraw(GLInfo glInfo) {
        synchronized (this) {
            super.onDraw(glInfo);
            GLCanvasImpl canvas = GLRenderer.getCanvas();
            canvas.onRenderPreDraw(glInfo);
            draw(canvas, glInfo);
            canvas.onRenderPostDraw();
            dump();
        }
    }

    public void invalidate() {
        mInvalidate = true;
    }


    public void onGone() {
        trimResources(GLRenderer.TRIM_MEMORY_UI_HIDDEN, false);
   }

    @Override
    public void trimResources(int level, boolean hasEglContext)  {
        synchronized (this) {
            super.trimResources(level, hasEglContext);
            if (level >= GLRenderer.TRIM_MEMORY_UI_HIDDEN) {
                BlurResult.recycle(mBlurResult, hasEglContext);
                mBlurResult = null;
            }
        }
    }

    private void dump() {
        if (GLRenderer.DEBUG_BLUR) {
            Log.i(GLRenderer.TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Log.i(GLRenderer.TAG, "Functor =" +this.getClass().getName());
            mDrawingParameters.dump();
            Log.i(GLRenderer.TAG, "progress = " +mProgress);
            Log.i(GLRenderer.TAG, "width = " +mSourceBounds.width());
            Log.i(GLRenderer.TAG, "height = " +mSourceBounds.height());
            Log.i(GLRenderer.TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }
    }
}
