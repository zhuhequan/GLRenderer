/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import com.android.common.renderer.effect.GLCanvas;

 public class FogRender extends PixelsRender {
    public FogRender(GLCanvas canvas) {
        super(canvas);
        mKey = FOG;
    }

    @Override
    public String getFragmentShader() {
        return FRAG;
    }

    private static final String FRAG =
        "precision mediump float; \n"    +

        "uniform sampler2D sTexture; \n" +
        "uniform float uAlpha;\n"+
        "varying vec2 vTexCoord; \n"     +

        "void main() \n" +
        "{ \n" +
        "    vec4 color = texture2D(sTexture, vTexCoord).rgba; \n" +
        "    vec3 fog = mix(vec3(0.5, 0.8, 0.5), color.rgb, 0.7); \n" +
        "    gl_FragColor = vec4(fog, color.a*uAlpha); \n" +
        "} \n";
 }
