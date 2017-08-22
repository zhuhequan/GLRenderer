/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import com.android.common.renderer.effect.GLCanvas;

 public class GrayRender extends PixelsRender {
    public GrayRender(GLCanvas canvas) {
        super(canvas);
        mKey = GRAY;
    }

    @Override
    public String getFragmentShader() {
        return FRAG;
    }

    private static final String FRAG =
            "precision mediump float; \n" +

            "uniform sampler2D sTexture; \n" +
            "uniform float uAlpha;\n"+
            "varying vec2 vTexCoord; \n" +

            "void main() { \n" +
            "    float alpha = texture2D(sTexture, vTexCoord).a*uAlpha; \n"+
            "    vec3 factor = vec3(0.299, 0.587, 0.114); \n" +
            "    vec3 color = texture2D(sTexture, vTexCoord).rgb; \n" +
            "    float gray = 0.0; \n" +
            "    gray = dot(color,factor); \n" +
            "    color = vec3(gray, gray, gray); \n" +
            "    gl_FragColor = vec4(color, alpha); \n" +
            "}";
 }
