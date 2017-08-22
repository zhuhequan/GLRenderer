package com.android.common.renderer.functor;
/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */

import android.graphics.Rect;
import android.opengl.GLES20;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLCanvasImpl;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.op.DrawGLSLOp;
import com.android.common.renderer.effect.render.GLSLRender;
import com.android.common.renderer.effect.render.Render;


public class DrawGLSLFunctor extends DrawGLFunctor {
    private String mGLSL;
    private DrawInfo mDrawInfo = new DrawInfo();
    private DrawGLSLOp mDrawGLSLOp = new DrawGLSLOp();
    private float mTime;

    //3个参数，shader里面如果需要额外的参数可以使用
    private float mParameter1;
    private float mParameter2;
    private float mParameter3;

    public DrawGLSLFunctor(String key, String glsl) {
        mGLSL = glsl;
        mEffectKey = key;
    }

    @Override
    public GLSLRender getRender(GLCanvas canvas) {
        Render render = canvas.getRender(mEffectKey);
        if (render == null) {
            render = new GLSLRender(GLRenderer.getCanvas(), mGLSL);
            render.setKey(mEffectKey);
            canvas.addRender(render);
        }
        return (GLSLRender)render;
    }

    protected void onDraw(GLInfo glInfo) {
        synchronized (this) {
            GLCanvasImpl canvas = GLRenderer.getCanvas();
            canvas.onRenderPreDraw(glInfo);
            GLSLRender render = getRender(canvas);
            if (render == null || !render.valid()) return;
            Rect r = mSourceBounds;

            int width = r.width();
            int height = r.height();
            int left = (int)(r.left + glInfo.transform[12]);
            int top = (int)(glInfo.viewportHeight - (r.top + height + glInfo.transform[13]));
            GLES20.glViewport(left, top, width, height);

            mDrawInfo.drawOp = mDrawGLSLOp;
            mDrawGLSLOp.x = left;
            mDrawGLSLOp.y = top;
            mDrawGLSLOp.width = width;
            mDrawGLSLOp.height = height;
            render.setTime(mTime);
            render.setParameter1(mParameter1);
            render.setParameter2(mParameter2);
            render.setParameter3(mParameter3);
            render.draw(mDrawInfo);
            mDrawInfo.drawOp = null;

            canvas.onRenderPostDraw();
        }

    }

    public void setTime(float time) {
        mTime = time;
    }

    public void setParameter1(float value) {
        mParameter1 = value;
    }
    public void setParameter2(float value) {
        mParameter2 = value;
    }
    public void setParameter3(float value) {
        mParameter3 = value;
    }

    public float getParameter1() {
        return mParameter1;
    }
    public float getParameter2() {
        return mParameter2;
    }
    public float getParameter3() {
        return mParameter3;
    }

    public float getTime() {
        return mTime;
    }

    public String getGLSL() {
        return mGLSL;
    }

    @Override
    public void setEffect(String key) {
    }
}
