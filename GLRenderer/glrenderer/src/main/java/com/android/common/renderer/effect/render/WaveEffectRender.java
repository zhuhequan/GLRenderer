/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.FrameBuffer;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.op.DrawOp;
import com.android.common.renderer.effect.op.DrawTextureOp;
public class WaveEffectRender extends Render {
    public static final String WAVE ="__wave";
    private int mProgress;
    private int mCols = 12;
    private int mRows = 12;
    private WaveRender mWaveRender;
    private DrawTextureOp mTextureElement = new DrawTextureOp();
    private DrawInfo mEffectInfo = new DrawInfo();
    public WaveEffectRender(GLCanvas canvas) {
        super(canvas);
        mKey = WAVE;
        mWaveRender = new WaveRender(canvas);
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }

    public void setGrid(int cols, int rows) {
        mCols = cols;
        mRows = rows;
    }

    @Override
    public boolean draw(DrawInfo drawInfo) {
        switch (drawInfo.drawOp.getId()) {

        case DrawOp.TEXTURE:
            drawTexture(drawInfo);
            return true;
        }
        return false;
    }

    private void drawTexture(DrawInfo drawInfo) {
        DrawTextureOp element = (DrawTextureOp)drawInfo.drawOp;
        int width = element.width;
        int height = element.height;
        FrameBuffer framebuffer = GLRenderer.getFrameBufferPool().get(width, height, true);
        framebuffer.onBind(mGLCanvas);
        mTextureElement.init(element.texture, 0, 0, width, height);
        mEffectInfo.clearFbo = true;
        mEffectInfo.depthTest = true;
        mEffectInfo.viewportWidth = width;
        mEffectInfo.viewportHeight = height;
        mEffectInfo.drawOp = mTextureElement;

        mGLCanvas.getState().push();
        mGLCanvas.getState().identityModelM();
        mGLCanvas.getState().identityTexM();
        mGLCanvas.getState().setFrameBufferId(framebuffer.getId());
        mWaveRender.setGrid(mCols, mRows);
        mWaveRender.setProgress(mProgress);
        mWaveRender.draw(mEffectInfo);
        mGLCanvas.getState().pop();

        mProgress = 0;
        mTextureElement.texture = null;
        mEffectInfo.reset();

        element.texture = framebuffer.getTexture();
        mGLCanvas.getRender(drawInfo.effectKey).draw(drawInfo);
        GLRenderer.getFrameBufferPool().put(framebuffer, true);
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        mWaveRender.trimResources(level, hasEglContext);
    }

    private class WaveRender extends MeshRender {
        private int mUniformAngleH;
        private float mAngle = 0;
        public WaveRender(GLCanvas canvas) {
            super(canvas);
        }

        @Override
        protected void initProgram() {
            super.initProgram();
            mUniformAngleH = GLES20.glGetUniformLocation(mProgram, "uAngle");
        }

        public void setProgress(int progress) {
            mAngle = progress * (float)Math.PI / 16;
        }

        @Override
        protected void initShader(DrawInfo drawInfo) {
            super.initShader(drawInfo);
            GLES20.glUniform1f(mUniformAngleH, mAngle);
        }

        @Override
        protected String getVertexShader() {
            return VERTEX;
        }

        private static final String VERTEX =
                "precision mediump float;   \n" +
                "uniform mat4 uMVPMatrix;   \n" +
                "uniform mat4 uSTMatrix;    \n" +
                "uniform float uAngle;      \n" +
                "attribute vec3 aPosition;  \n" +
                "attribute vec2 aTexCoord;  \n" +
                "varying vec2 vTexCoord;    \n" +
                "void main() {              \n" +
                "    if ((abs(abs(aPosition.x) - 1.0) < 0.01 && abs(abs(aPosition.y) - 1.0) > 0.01) ||  \n" +
                "        (abs(abs(aPosition.y) - 1.0) < 0.01 && abs(abs(aPosition.x) - 1.0) > 0.01) || \n" +
                "        (abs(abs(aPosition.y) - 1.0) < 0.01 && abs(abs(aPosition.x) - 1.0) < 0.01)) { \n" +
                "        gl_Position = uMVPMatrix * vec4(aPosition, 1);          \n"+
                "    } else {  \n"+
                "        float angleSpanH = 2.0*3.14159265;                 \n" +
                "        float currAngle = uAngle + (aPosition.x + 1.0)*angleSpanH; \n" +
                "        float tz = sin(currAngle)*0.1;      \n" +
                "        gl_Position = uMVPMatrix * vec4(aPosition.x, aPosition.y, tz, 1); \n" +
                "    }  \n"+
                "    vTexCoord = (uSTMatrix * vec4(aTexCoord, 0, 1)).st;           \n" +
                "} \n" ;
     }
}
