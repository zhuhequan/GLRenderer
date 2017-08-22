/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import com.android.common.renderer.effect.GLCanvas;

 public class VividRender extends PixelsRender {
    public VividRender(GLCanvas canvas) {
        super(canvas);
        mKey = VIVID;
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

            "float BlendOverlayf(float base, float blend) \n" +
            "{ \n" +
            "  return (base < 0.5 ? (2.0 * base * blend) : (1.0 - 2.0 * (1.0 - base) * (1.0 - blend))); \n" +
            "} \n" +

            "vec3 BlendOverlay(vec3 base, vec3 blend) \n" +
            "{ \n" +
            "  return vec3(BlendOverlayf(base.r, blend.r), BlendOverlayf(base.g, blend.g), BlendOverlayf(base.b, blend.b)); \n" +
            "} \n" +

            "vec3 HardLight(vec3 base, vec3 blend) \n" +
            "{ \n" +
            "  return BlendOverlay(blend, base); \n" +
            "} \n" +

            "void main() \n" +
            "{ \n" +
            "   vec4 color = texture2D(sTexture, vTexCoord); \n" +
            "   vec3 tmpColor = HardLight(color.rgb, color.rgb); \n" +
            "   gl_FragColor = vec4(mix(color.rgb, tmpColor, 0.7), color.a*uAlpha); \n" +
            "} \n";
 }
