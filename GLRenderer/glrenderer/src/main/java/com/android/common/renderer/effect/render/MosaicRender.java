/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.DrawInfo;

public class MosaicRender extends ConvolutionRender {
    public MosaicRender(GLCanvas canvas) {
        super(canvas);
        mKey = MOSAIC;
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
            "precision highp float; \n" +
            "uniform sampler2D sTexture; \n" +
            "uniform float uAlpha;\n"+
            "uniform vec2 uStep; \n" +
            "varying vec2 vTexCoord; \n" +

            "vec3 mosaic() { \n" +
            "    vec2 step = uStep; \n" +
            "    vec2 st0 = (step.x < step.y) ? \n" +
            "                vec2(0.02, 0.02 * step.y/step.x) : \n" +
            "                vec2(0.02*step.x/step.y, 0.02); \n" +
            "    vec2 st = floor(vTexCoord/st0) * st0; \n" +
            "    vec2 st1 = st + st0*0.5; \n" +
            "    return 0.25 * (texture2D(sTexture, st).rgb + \n" +
            "             texture2D(sTexture, st1).rgb + \n" +
            "             texture2D(sTexture, vec2(st.s,st1.t)).rgb + \n" +
            "             texture2D(sTexture, vec2(st1.s,st.t)).rgb); \n" +
            "} \n" +

            "void main() \n" +
            "{             \n"+
            "  gl_FragColor.rgb = mosaic(); \n" +
            "  gl_FragColor.a = uAlpha;\n"+
            "}";
}
