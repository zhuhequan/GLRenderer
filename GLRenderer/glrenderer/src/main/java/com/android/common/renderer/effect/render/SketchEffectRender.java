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
public class SketchEffectRender extends RenderGroup {
    private SketchRender mSketchRender;
    private Gaussian2DRender mGaussian2DRender;
    private DrawTextureOp mTextureElement = new DrawTextureOp();
    private DrawInfo mEffectInfo = new DrawInfo();
    public SketchEffectRender(GLCanvas canvas) {
        super(canvas);
        mKey = SKETCH;
        mSketchRender = new SketchRender(canvas);
        mGaussian2DRender = new Gaussian2DRender(canvas);
        mRenders.add(mGaussian2DRender);
        mRenders.add(mSketchRender);
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
        DrawTextureOp element = (DrawTextureOp)drawInfo.drawOp;
        mEffectInfo.viewportWidth = element.width;
        mEffectInfo.viewportHeight = element.height;
        mEffectInfo.drawOp = mTextureElement;
        FrameBuffer frameBuffer = GLRenderer.getFrameBufferPool().get(element.width, element.height, true);
        frameBuffer.onBind(mGLCanvas);
        mGLCanvas.getState().push();
        mGLCanvas.getState().identityModelM();
        mGLCanvas.getState().identityTexM();
        mGLCanvas.getState().setFrameBufferId(frameBuffer.getId());
        mTextureElement.init(element.texture, 0, 0, element.width, element.height);
        mGaussian2DRender.draw(mEffectInfo);
        mGLCanvas.getState().pop();

        element.texture = frameBuffer.getTexture();
        mSketchRender.draw(drawInfo);

        GLRenderer.getFrameBufferPool().put(frameBuffer, true);

        mTextureElement.texture = null;
        mEffectInfo.reset();
    }

    private static class SketchRender extends ConvolutionRender {
        public SketchRender(GLCanvas canvas) {
            super(canvas);
        }

        @Override
        public String getFragmentShader() {
            return FRAG;
        }

        @Override
        protected void initShader(DrawInfo drawInfo) {
            super.initShader(drawInfo);
            GLES20.glUniform2f(mUniformStepH,
                    1.0f/drawInfo.drawOp.width,
                    1.0f/drawInfo.drawOp.height);
        }

        private static final String FRAG =
                "precision mediump float; \n" +

                "uniform vec2 uStep; \n" +
                "uniform float uAlpha;\n"+
                "uniform sampler2D sTexture; \n" +
                "varying vec2 vTexCoord; \n" +

                "float rgb2gray(vec4 color) { \n" +
                "    return dot(color, vec4(0.299, 0.587, 0.114, 0.0)); \n" +
                "} \n" +

                "void main() \n" +
                "{ \n" +
                "    vec4 bigStep = vec4(uStep, uStep); \n" +
                "    float sample = 0.0; \n" +
                "    sample  = 0.0448 * rgb2gray(texture2D(sTexture, vTexCoord - bigStep.pq)); \n" +
                "    sample += 0.0468 * rgb2gray(texture2D(sTexture, vTexCoord - bigStep.sq)); \n" +
                "    sample += 0.0564 * rgb2gray(texture2D(sTexture, vTexCoord - vec2(0.0, bigStep.q))); \n" +
                "    sample += 0.0468 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(bigStep.s, -bigStep.q))); \n" +
                "    sample += 0.0448 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(bigStep.p, -bigStep.q))); \n" +
                "    sample += 0.0468 * rgb2gray(texture2D(sTexture, vTexCoord - bigStep.pt)); \n" +
                "    sample += 0.3167 * rgb2gray(texture2D(sTexture, vTexCoord - bigStep.st)); \n" +
                "    sample += 0.7146 * rgb2gray(texture2D(sTexture, vTexCoord - vec2(0.0, bigStep.t))); \n" +
                "    sample += 0.3167 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(bigStep.s, -bigStep.t))); \n" +
                "    sample += 0.0468 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(bigStep.p, -bigStep.t))); \n" +
                "    sample += 0.0564 * rgb2gray(texture2D(sTexture, vTexCoord - vec2(bigStep.p, 0.0))); \n" +
                "    sample += 0.7146 * rgb2gray(texture2D(sTexture, vTexCoord - vec2(bigStep.s, 0.0))); \n" +
                "    sample -= 4.9048 * rgb2gray(texture2D(sTexture, vTexCoord)); \n" +
                "    sample += 0.7146 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(bigStep.s, 0.0))); \n" +
                "    sample += 0.0564 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(bigStep.p, 0.0))); \n" +
                "    sample += 0.0468 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(-bigStep.p, bigStep.t))); \n" +
                "    sample += 0.3167 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(-bigStep.s, bigStep.t))); \n" +
                "    sample += 0.7146 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(0.0, bigStep.t))); \n" +
                "    sample += 0.3167 * rgb2gray(texture2D(sTexture, vTexCoord + bigStep.st)); \n" +
                "    sample += 0.0468 * rgb2gray(texture2D(sTexture, vTexCoord + bigStep.pt)); \n" +
                "    sample += 0.0448 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(-bigStep.p, bigStep.q))); \n" +
                "    sample += 0.0468 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(-bigStep.s, bigStep.q))); \n" +
                "    sample += 0.0564 * rgb2gray(texture2D(sTexture, vTexCoord + vec2(0.0, bigStep.q))); \n" +
                "    sample += 0.0468 * rgb2gray(texture2D(sTexture, vTexCoord + bigStep.sq)); \n" +
                "    sample += 0.0448 * rgb2gray(texture2D(sTexture, vTexCoord + bigStep.pq)); \n" +
                "    sample = 1.0 - 3.0 * sample; \n" +
                "    sample = clamp(sample, 0.0, 1.0); \n" +
                "    gl_FragColor.rgb = vec3(sample); \n" +
                "    gl_FragColor.a = uAlpha; \n" +
                "}";
    }

    private static  class Gaussian2DRender extends ConvolutionRender {
        public Gaussian2DRender(GLCanvas canvas) {
            super(canvas);
        }

        @Override
        public String getFragmentShader() {
            return FRAG;
        }

        @Override
        protected void initShader(DrawInfo drawInfo) {
            super.initShader(drawInfo);
            GLES20.glUniform2f(mUniformStepH, 1.0f/drawInfo.drawOp.width,
                    1.0f/drawInfo.drawOp.height);
        }

        private static final String FRAG =
               "precision mediump float; \n" +

               "uniform vec2 uStep; \n" +
               "uniform sampler2D sTexture; \n" +
               "varying vec2 vTexCoord; \n" +

               "void main() { \n" +
               "    vec2 step = uStep; \n" +
               "    vec3 a = vec3(0.0113,0.0838,0.6193); \n" +
               "    vec3 sum; \n" +
               "    sum = texture2D(sTexture,  vTexCoord - step).rgb * a.x; \n" +
               "    sum += texture2D(sTexture, vTexCoord + vec2(0.0, -step.y)).rgb * a.y; \n" +
               "    sum += texture2D(sTexture, vTexCoord + vec2(step.x, -step.y)).rgb * a.x; \n" +
               "    sum += texture2D(sTexture, vTexCoord + vec2(step.x, 0.0)).rgb * a.y; \n" +
               "    sum += texture2D(sTexture, vTexCoord).rgb * a.z; \n" +
               "    sum += texture2D(sTexture, vTexCoord + vec2(-step.x, 0.0)).rgb * a.y; \n" +
               "    sum += texture2D(sTexture, vTexCoord + vec2(-step.x, step.y)).rgb * a.x; \n" +
               "    sum += texture2D(sTexture, vTexCoord + vec2(0.0, step.y)).rgb * a.y; \n" +
               "    sum += texture2D(sTexture, vTexCoord + step).rgb * a.x; \n" +
               "    gl_FragColor.rgb = sum; \n" +
               "    gl_FragColor.a = 1.0; \n" +
               " } ";

    }

}
