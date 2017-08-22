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
public class RCEffectRender extends Render {
    public static final String  ROUND_CORNER ="__round_corner";
    private RCRender mRCRender;
    private DrawTextureOp mTextureElement = new DrawTextureOp();
    private DrawInfo mEffectInfo = new DrawInfo();
    public RCEffectRender(GLCanvas canvas) {
        super(canvas);
        mKey = ROUND_CORNER;
        mRCRender = new RCRender(canvas);
    }

    public void setRadius(float radius) {
        mRCRender.setRadius(radius);
    }

    @Override
    public boolean draw(DrawInfo drawInfo) {
        switch (drawInfo.drawOp.getId()) {

        case DrawOp.TEXTURE:
            drawTexure(drawInfo);
            return true;
        }
        return false;
    }

    private void drawTexure(DrawInfo drawInfo) {
        if (drawInfo.effectKey.equals(NONE)) {
            mRCRender.draw(drawInfo);
        } else {
            DrawTextureOp element = (DrawTextureOp)drawInfo.drawOp;
            mEffectInfo.viewportWidth = element.width;
            mEffectInfo.viewportHeight = element.height;
            mEffectInfo.drawOp = mTextureElement;
            FrameBuffer frameBuffer = GLRenderer.getFrameBufferPool().get(element.width, element.height,true);
            frameBuffer.onBind(mGLCanvas);

            mGLCanvas.getState().push();
            mGLCanvas.getState().identityModelM();
            mGLCanvas.getState().identityTexM();
            mGLCanvas.getState().setFrameBufferId(frameBuffer.getId());
            mTextureElement.init(element.texture, 0, 0, element.width, element.height);
            mGLCanvas.getRender(drawInfo.effectKey).draw(mEffectInfo);
            mGLCanvas.getState().pop();

            element.texture = frameBuffer.getTexture();
            mRCRender.draw(drawInfo);

            GLRenderer.getFrameBufferPool().put(frameBuffer, true);

            mTextureElement.texture = null;
            mEffectInfo.reset();
        }
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        super.trimResources(level, hasEglContext);
        mRCRender.trimResources(level, hasEglContext);
    }

    private static class RCRender extends ConvolutionRender {
        protected int mUniformRadius;
        private float mRadius = 20;
        public RCRender(GLCanvas canvas) {
            super(canvas);
            mKey = NONE;
        }

        @Override
        public String getFragmentShader() {
            return FRAG;
        }

        @Override
        protected void initProgram() {
            super.initProgram();
            mUniformRadius = GLES20.glGetUniformLocation(mProgram, "uRadius");
        }

        public void setRadius(float radius) {
            mRadius = radius;
        }

        @Override
        protected void initShader(DrawInfo drawInfo) {
            super.initShader(drawInfo);
            GLES20.glUniform2f(mUniformStepH, drawInfo.drawOp.width, drawInfo.drawOp.height);
            GLES20.glUniform1f(mUniformRadius, mRadius);
        }

        private static final String FRAG =
                "precision highp float;   \n" +
                "varying vec2 vTexCoord;   \n" +
                "uniform float uAlpha;\n"+
                "uniform sampler2D sTexture; \n" +
                "uniform vec2 uStep; \n" +
                "uniform float uRadius;\n" +

                "bool isInRoundRect(float x, float y, float width, float height, float radius) { \n" +
                "    if (x < radius && y < radius) { \n" +
                "        return sqrt((x-radius)*(x-radius) + (y-radius)*(y-radius)) <= radius;\n" +
                "    } else if ( x > (width - radius) && y < radius) { \n" +
                "        return sqrt((x-(width - radius))*(x-(width - radius)) + (y-radius)*(y-radius)) <= radius;\n" +
                "    } else if (x < radius && y > (height - radius)) { \n" +
                "        return sqrt((x-radius)*(x-radius) + (y-(height - radius))*(y-(height - radius))) <= radius; \n" +
                "    } else if (x > (width - radius) && y > (height - radius)) { \n" +
                "        return sqrt((x-(width - radius))*(x-(width - radius)) + (y-(height - radius))*(y-(height - radius))) <= radius; \n" +
                "    } else {\n" +
                "        return true; \n" +
                "    }\n" +
                "    return true;\n" +
                "} \n"+

                "void main()  \n" +
                "{   \n" +
                "    if (!isInRoundRect(vTexCoord.x*uStep.x, vTexCoord.y*uStep.y, uStep.x, uStep.y, uRadius)) {" +
                "        discard; \n" +
                "        return; \n" +
                "    }\n" +
                "    gl_FragColor = texture2D(sTexture, vTexCoord); \n" +
                "    gl_FragColor.a *= uAlpha;\n" +
                "} \n";
     }
}
