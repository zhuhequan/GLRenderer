/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import com.android.common.renderer.effect.GLCanvas;

 public class BrannanRender extends PixelsRender {
    public BrannanRender(GLCanvas canvas) {
        super(canvas);
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

            "vec3 RGBToHSL(vec3 color) \n" +
            "{ \n" +
            "    vec3 hsl; \n" +
            "    float fmin = min(min(color.r, color.g), color.b); \n" +
            "    float fmax = max(max(color.r, color.g), color.b); \n" +
            "    float delta = fmax - fmin; \n" +
            "    hsl.z = (fmax + fmin) / 2.0; \n" +
            "   if (delta == 0.0) \n" +
            "    { \n" +
            "        hsl.x = 0.0; \n" +
            "       hsl.y = 0.0; \n" +
            "    } \n" +
            "   else \n" +
            "      { \n" +
            "       if (hsl.z < 0.5) hsl.y = delta / (fmax + fmin); \n" +
            "       else             hsl.y = delta / (2.0 - fmax - fmin); \n" +
            "       float deltaR = (((fmax - color.r) / 6.0) + (delta / 2.0)) / delta; \n" +
            "       float deltaG = (((fmax - color.g) / 6.0) + (delta / 2.0)) / delta; \n" +
            "       float deltaB = (((fmax - color.b) / 6.0) + (delta / 2.0)) / delta; \n" +
            "       if (color.r == fmax ) hsl.x = deltaB - deltaG; // Hue \n" +
            "       else if (color.g == fmax) hsl.x = (1.0 / 3.0) + deltaR - deltaB; // Hue \n" +
            "       else if (color.b == fmax) hsl.x = (2.0 / 3.0) + deltaG - deltaR; // Hue \n" +
            "       if (hsl.x < 0.0) hsl.x += 1.0; // Hue \n" +
            "       else if (hsl.x > 1.0) hsl.x -= 1.0; // Hue \n" +
            "   } \n" +
            "   return hsl; \n" +
            "} \n" +

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

            "vec3 SaturateAdjustment(vec3 color) \n" +
            "{ \n" +
            "  vec3 hsl = RGBToHSL(color); \n" +
            "  float sOffset = -0.29; \n" +
            "  color.r = color.r + (color.r - hsl.z) * sOffset; \n" +
            "  color.g = color.g + (color.g - hsl.z) * sOffset; \n" +
            "  color.b = color.b + (color.b - hsl.z) * sOffset; \n" +
            "  return color; \n" +
            "} \n" +

            "vec4 Desaturte(vec3 color, float Desaturation) \n" +
            "{ \n" +
            "  vec3 grayXfer = vec3(0.3, 0.59, 0.11); \n" +
            "  vec3 gray = vec3(dot(grayXfer, color)); \n" +
            "  return vec4(mix(color, gray, Desaturation), 1.0); \n" +
            "}\n" +

            "float BlendOverlayf(float base, float blend) \n" +
            "{ \n" +
            "   return (base < 0.5 ? (2.0 * base * blend) : (1.0 - 2.0 * (1.0 - base) * (1.0 - blend))); \n" +
            "} \n" +

            "vec3 BlendOverlay(vec3 base, vec3 blend) \n" +
            "{ \n" +
            "   vec3 destColor; \n" +
            "   destColor.r = BlendOverlayf(base.r, blend.r); \n" +
            "   destColor.g = BlendOverlayf(base.g, blend.g); \n" +
            "   destColor.b = BlendOverlayf(base.b, blend.b); \n" +
            "   return destColor; \n" +
            "} \n" +

            "vec3 ContrastAndBrightness(vec3 color, float brt, float con) \n" +
            "{ \n" +
            "   vec3 tmp = color * brt; \n" +
            "   tmp.rgb -= 0.5; \n" +
            "   tmp.rgb *= con; \n" +
            "   tmp.rgb += 0.5; \n" +
            "   return tmp; \n" +
            "} \n" +

            "void main() \n" +
            "{ \n" +
            "   vec4 color = texture2D(sTexture, vTexCoord); \n" +
            "   vec3 saturateAdjustmentColor; \n" +
            "   saturateAdjustmentColor = SaturateAdjustment(color.rgb); \n" +
            "   vec3 desaturateColor = Desaturte(color.rgb, 1.0).rgb; \n" +
            "   vec3 blendColor = BlendOverlay(saturateAdjustmentColor, desaturateColor); \n" +
            "   blendColor = mix(saturateAdjustmentColor, blendColor, 0.34); \n" +
            "   vec3 levelAdjustmentColor = LevelsControl(blendColor.rgb, 0.0, 1.0, 1.0, 9.0/255.0, 1.0); \n" +
            "   levelAdjustmentColor.r = SingleChannelLevelControl(levelAdjustmentColor.r, 0.0, 1.0, 228.0/255.0, 25.0/255.0, 1.0); \n" +
            "   levelAdjustmentColor.g = SingleChannelLevelControl(levelAdjustmentColor.g, 0.0, 1.0, 1.0, 3.0/255.0, 1.0); \n" +
            "   levelAdjustmentColor.b = SingleChannelLevelControl(levelAdjustmentColor.b, 0.0, 1.0, 237.0/255.0, 12.0/255.0, 1.0); \n" +
            "   vec3 conAndBrtAdjustmentColor = ContrastAndBrightness(levelAdjustmentColor, 0.94, 1.0); \n" +
            "   vec3 fillColor = vec3(1.0, 248.0/255.0, 242.0/255.0); \n" +
            "   blendColor = conAndBrtAdjustmentColor * fillColor; \n" +
            "   levelAdjustmentColor = LevelsControl(blendColor, 11.0/255.0, 0.91, 1.0, 7.0/255.0, 1.0); \n" +
            "   levelAdjustmentColor.r = SingleChannelLevelControl(levelAdjustmentColor.r, 0.0, 1.0, 1.0, 10.0/255.0, 1.0); \n" +
            "   levelAdjustmentColor.g = SingleChannelLevelControl(levelAdjustmentColor.g, 0.0, 1.0, 224.0 / 255.0, 3.0/255.0, 1.0); \n" +
            "   levelAdjustmentColor.b = SingleChannelLevelControl(levelAdjustmentColor.b, 0.0, 0.94, 1.0, 18.0/255.0, 1.0); \n" +
            "   conAndBrtAdjustmentColor = ContrastAndBrightness(levelAdjustmentColor, 0.983, 0.99); \n" +
            "   conAndBrtAdjustmentColor = ContrastAndBrightness(levelAdjustmentColor, 1.0, 1.03); \n" +
            "   conAndBrtAdjustmentColor = ContrastAndBrightness(levelAdjustmentColor, 1.03, 1.04); \n" +
            "   gl_FragColor = vec4(conAndBrtAdjustmentColor, color.a*uAlpha); return;\n" +
            "} \n";
 }
