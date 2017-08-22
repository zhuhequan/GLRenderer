/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.graphics.Color;
import android.opengl.GLES20;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.texture.Texture;

public class BlurFilterRender extends PixelsRender {
    public static final String KEY = "blur_filter";
    private int mUniformIntensityH;
    private int mUniformTextureH2;
    private int mUniformFilterColor;
    private int mUniformHasTexture;
    private float mIntensity = 1.0f;
    private int mFilterColor = 0;
    private Texture mOrigTexture;
    public static BlurFilterRender getInstace(GLCanvas canvas) {
        Render render = canvas.getRender(BlurFilterRender.KEY);
        if (render == null) {
            render = new BlurFilterRender(canvas);
            canvas.addRender(render);
        }
        return (BlurFilterRender)render;
    }

    public BlurFilterRender(GLCanvas canvas) {
        super(canvas);
        mKey = KEY;
    }

    @Override
    protected void initProgram() {
        super.initProgram();
        mUniformFilterColor = GLES20.glGetUniformLocation(mProgram, "uFilterColor");
        mUniformIntensityH = GLES20.glGetUniformLocation(mProgram, "uIntensity");
        mUniformTextureH2 = GLES20.glGetUniformLocation(mProgram, "sTexture2");
        mUniformHasTexture = GLES20.glGetUniformLocation(mProgram, "uHasTexture");
    }

    public void setFilterColor(int color) {
        mFilterColor = color;
    }

    public void setIntensity(float intensity) {
        mIntensity = intensity;
    }

    public void setOrigTexture(Texture texture) {
        mOrigTexture = texture;
    }

    @Override
    protected void initShader(DrawInfo drawInfo) {
        super.initShader(drawInfo);
        GLES20.glUniform4f(mUniformFilterColor,
                (float)Color.red(mFilterColor)/255,
                (float)Color.green(mFilterColor)/255,
                (float)Color.blue(mFilterColor)/255,
                (float)Color.alpha(mFilterColor)/255);
        GLES20.glUniform1f(mUniformIntensityH, mIntensity);
        GLES20.glUniform1i(mUniformTextureH2, 1);

        if (mOrigTexture != null) {
            GLES20.glUniform1i(mUniformHasTexture, 1);
            bindTexture(mOrigTexture, GLES20.GL_TEXTURE1);
        } else {
            GLES20.glUniform1i(mUniformHasTexture, 0);
        }
    }

    @Override
    protected String getFragmentShader() {
        return FRAG;
    }

    private static final String FRAG=
            "precision mediump float;\n"+
                    "uniform sampler2D sTexture;\n"+
                    "uniform sampler2D sTexture2;\n"+
                    "uniform int uHasTexture;\n"+
                    "uniform float uAlpha;\n"+
                    "uniform float uIntensity;\n"+
                    "uniform vec4 uFilterColor;\n"+
                    "varying vec2 vTexCoord; \n" +

                    "void main() { \n"+

                    "    if (uHasTexture == 1) {\n" +
                    "        vec3 color = (texture2D(sTexture,  vTexCoord).rgb*uAlpha +\n" +
                    "                     texture2D(sTexture2, vTexCoord).rgb*(1.0-uAlpha)); \n" +
                    "        gl_FragColor.rgb = (uFilterColor.rgb*uFilterColor.a + color*(1.0-uFilterColor.a))*uIntensity;\n"+
                    "        gl_FragColor.a = 1.0; \n" +
                    "     } else {" +
                    "        vec3 color = texture2D(sTexture,  vTexCoord).rgb;\n" +
                    "        gl_FragColor.rgb = (uFilterColor.rgb*uFilterColor.a + color*(1.0-uFilterColor.a))*uIntensity;\n"+
                    "        gl_FragColor.a = uAlpha;\n"+
                    "     }\n"+
                    "}\n";
}
