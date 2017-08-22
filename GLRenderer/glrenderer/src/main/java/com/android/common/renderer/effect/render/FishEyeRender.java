/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.DrawInfo;

public class FishEyeRender extends ConvolutionRender {
    private int mUniformFH;
    private int mUniformInvMaxDistH;
    private float mInvMaxDist;
    private float mF;
    private int mTexW;
    private int mTexH;
    public FishEyeRender(GLCanvas canvas) {
        super(canvas);
        mKey = Render.FISHEYE;
    }

    @Override
    public String getFragmentShader() {
        return FRAG;
    }

    @Override
    protected void initProgram() {
        super.initProgram();
        mUniformFH = GLES20.glGetUniformLocation(mProgram, "uF");
        mUniformInvMaxDistH = GLES20.glGetUniformLocation(mProgram, "uInvMaxDist");
    }

    private void update(int w, int h) {
        if (mTexW != w || mTexH!= h) {
            mTexW = w;
            mTexH = h;
            int d = w > h ? h : w;
            float L = (float)Math.sqrt(mTexW*mTexW + mTexH*mTexH);
            if (d > 1080) {
                mStepX = 2.5f/mTexW;
                mStepY = 2.5f/mTexH;
                mF = 6.0f*L/35.0f;
            } else if (d > 720) {
                mStepX = 1.5f/mTexW;
                mStepY = 1.5f/mTexH;
                mF = 7.0f*L/35.0f;
            } else {
                mStepX = 1.0f/mTexW;
                mStepY = 1.0f/mTexH;
                mF = 10.0f*L/35.0f;
            }

            mInvMaxDist = 2.0f/L;
        }
    }

    @Override
    protected void initShader(DrawInfo drawInfo) {
        super.initShader(drawInfo);
        update(drawInfo.drawOp.width, drawInfo.drawOp.height);
        GLES20.glUniform1f(mUniformFH, mF);
        GLES20.glUniform1f(mUniformInvMaxDistH, mInvMaxDist);
        GLES20.glUniform2f(mUniformStepH, mStepX, mStepY);
    }

    private static final String FRAG =
            "precision highp float; \n" +

            "uniform sampler2D sTexture; \n" +
            "uniform float uAlpha;\n"+
            "varying vec2 vTexCoord; \n" +
            "uniform vec2 uStep; \n" +
            "uniform float uInvMaxDist; \n" +
            "uniform float uF; \n" +

            "vec3 fisheye() { \n" +
            "    const float slope = 20.0;               // vignette slope  \n" +
            "    const float shade = 0.85;               // vignette shading  \n" +
            "    const float range = 0.6;               // 0.6 - 1.3 \n" +
            "    const float zoom = 0.3;               // smaller zoom means bigger image \n" +
            "    vec2 coord = (vTexCoord - 0.5) / uStep; // convert to world coordinate  \n" +
            "    float dist = length(coord); // distance to the center \n" +
            "    float lumen = shade / (1.0 + exp((dist * uInvMaxDist - range) * slope)) + (1.0 - shade); \n" +
            "    float t = zoom*dist/uF; \n" +
            "    float theta = asin(t)*2.0; \n" +
            "    float r = uF * tan(theta); \n" +
            "    float angle = atan(coord.y, coord.x); \n" +
            "    vec2 newCoord = vec2(cos(angle), sin(angle))*uStep*r+0.5; \n" +
            "    return texture2D(sTexture, newCoord).rgb;  \n" +
            "   // return texture2D(sTexture, newCoord).rgb * lumen; \n" +
            "} \n" +
            "void main() { \n" +
            "    gl_FragColor.rgb = fisheye(); \n" +
            "    gl_FragColor.a = texture2D(sTexture,vTexCoord).a*uAlpha; \n" +
            "}";
}

