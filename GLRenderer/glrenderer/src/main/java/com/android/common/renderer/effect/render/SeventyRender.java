/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import com.android.common.renderer.effect.GLCanvas;

 public class SeventyRender extends PixelsRender {
    public SeventyRender(GLCanvas canvas) {
        super(canvas);
        mKey = SEVENTY;
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

            "vec3 GammaCorrection(vec3 color, float gamma) \n" +
            "{ \n" +
            "   return vec3(pow(color.r, 1.0 / gamma), pow(color.g, 1.0 / gamma), pow(color.b, 1.0 / gamma)); \n" +
            "} \n" +

            "float SingleChannelGammaCorrection(float value, float gamma) \n" +
            "{ \n" +
            "  return pow(value, 1.0 / gamma); \n" +
            "} \n" +

            "vec3 LevelsControlInputRange(vec3 color, float minInput, float maxInput) \n" +
            "{ \n" +
            "  return min(max(color - vec3(minInput), vec3(0.0)) / (vec3(maxInput) - vec3(minInput)), vec3(1.0)); \n" +
            "} \n" +

            "float SingleChannelControlInputRange(float value, float minInput, float maxInput) \n" +
            "{ \n" +
            "   return min(max(value - minInput, 0.0) / (maxInput - minInput), 1.0); \n" +
            "} \n" +

            "vec3 LevelsControlInput(vec3 color, float minInput, float gamma, float maxInput) \n" +
            "{ \n" +
            "   return GammaCorrection(LevelsControlInputRange(color, minInput, maxInput), gamma); \n" +
            "} \n" +

            "float SingleChannelControlInput(float value, float minInput, float gamma, float maxInput) \n" +
            "{ \n" +
            "   return SingleChannelGammaCorrection(SingleChannelControlInputRange(value, minInput, maxInput), gamma); \n" +
            "} \n" +

            "vec3 LevelsControlOutputRange(vec3 color, float minOutput, float maxOutput) \n" +
            "{ \n" +
            "   return mix(vec3(minOutput), vec3(maxOutput), color); \n" +
            "} \n" +

            "float SingleLevelsControlOutputRange(float value, float minOutput, float maxOutput) \n" +
            "{ \n" +
            "   return mix(minOutput, maxOutput, value); \n" +
            "} \n" +

            "vec3 LevelsControl(vec3 color, float minInput, float gamma, float maxInput, float minOutput, float maxOutput) \n" +
            "{ \n" +
            "   vec3 tmpColor = LevelsControlInput(color, minInput, gamma, maxInput); \n" +
            "   return LevelsControlOutputRange(tmpColor, minOutput, maxOutput); \n" +
            "} \n" +

            "float SingleChannelLevelControl(float value, float minInput, float gamma, float maxInput, float minOutput, float maxOutput) \n" +
            "{ \n" +
            "   float tmpValue = SingleChannelControlInput(value, minInput, gamma, maxInput); \n" +
            "   return SingleLevelsControlOutputRange(tmpValue, minOutput, maxOutput); \n" +
            "} \n" +

            "void main() \n" +
            "{ \n" +
            "   vec4 color = texture2D(sTexture, vTexCoord); \n" +
            "   color.rgb = LevelsControl(color.rgb, 30.0 / 255.0, 0.75, 211.0 / 255.0, 73.0 / 255.0, 236.0 / 255.0); \n" +
            "   color.r = SingleChannelLevelControl(color.r, 0.0, 1.38, 236.0 / 255.0, 30.0 / 255.0, 231.0/255.0); \n " +
            "   color.g = SingleChannelLevelControl(color.g, 0.0, 0.79, 236.0/255.0, 46.0/255.0, 221.0/255.0); \n " +
            "   color.b = SingleChannelLevelControl(color.b, 5.0/255.0, 0.96, 221.0/255.0, 3.0/255.0, 241.0/255.0); \n " +
            "   gl_FragColor.rgb = color.rgb; \n" +
            "   gl_FragColor.a *= uAlpha; \n" +
            "} \n";
 }
