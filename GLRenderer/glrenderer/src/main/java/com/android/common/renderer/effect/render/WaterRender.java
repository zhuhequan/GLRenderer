/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.DrawInfo;

 public class WaterRender extends ConvolutionRender {
    public WaterRender(GLCanvas canvas) {
        super(canvas);
        mKey = WATER;
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
            "precision mediump float;   \n" +
            "varying vec2 vTexCoord;   \n" +
            "uniform float uAlpha;\n"+
            "uniform sampler2D sTexture; \n" +
            "uniform vec2 uStep; \n" +
            "vec2 water() {  \n" +
            "  float s1 = (uStep.x < uStep.y) ? 0.01 : 0.01 * uStep.x / uStep.y; \n" +
            "  float t1 = (uStep.x < uStep.y) ? 0.01 * uStep.y / uStep.x : 0.01;    \n" +
            "  float s2 = (uStep.x < uStep.y) ? 30.0 : 30.0 * uStep.x / uStep.y; \n" +
            "  float t2 = (uStep.x < uStep.y) ? 30.0 * uStep.y / uStep.x : 30.0;   \n" +
            "  float s = sin(s2*vTexCoord.t) * s1; \n" +
            "  float t = sin(t2*vTexCoord.s) * t1;\n" +
            "  return vec2(s,t);} \n" +
            "void main()  \n" +
            "{   \n" +
            "    gl_FragColor = texture2D(sTexture, vTexCoord + water()); \n" +
            "    gl_FragColor.a *= uAlpha;\n" +
            "}";
 }
