/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2016-01-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.RendererUtils;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLES31Utils;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.ShaderUtils;
import com.android.common.renderer.effect.texture.Texture;

public class ProgressBlurRender extends AbstractBlurRender {
    public static final String FAST_BLUR = "fast_blur";
    private static int TEXTURE_FORMAT = GLES31Utils.GL_RGBA16F;
    private static final int BLUR_WORK_SIZE = 16;
    private int mSize[] = new int[3];
    protected int mProgramH = 0;
    protected int mUniformRadiusH;
    protected int mProgramV = 0;
    protected int mUniformRadiusV;
    protected int mProgramS = 0;
    protected int mUniformRadiusS;
    private int mUniformTextureS;
    private boolean mNotSupportMemoryBarrier;
    public ProgressBlurRender(GLCanvas canvas) {
        super(canvas);
        mProgramH = ShaderUtils.createComputeProgram(getBlurShader(false));
        mUniformRadiusH = GLES20.glGetUniformLocation(mProgramH, "uRadius");

        mProgramV = ShaderUtils.createComputeProgram(getBlurShader(true));
        mUniformRadiusV = GLES20.glGetUniformLocation(mProgramV, "uRadius");

        mProgramS = ShaderUtils.createComputeProgram(scaleShader);
        mUniformTextureS = GLES20.glGetUniformLocation(mProgramS, "uInputImage");
        mUniformRadiusS = GLES20.glGetUniformLocation(mProgramS, "uRadius");

        mKey = FAST_BLUR;
        String GPUType = GLES20.glGetString(GLES20.GL_RENDERER);
        mNotSupportMemoryBarrier = "Adreno (TM) 430".equals(GPUType);
    }

    public static ProgressBlurRender getInstance(GLCanvas canvas) {
        Render render = canvas.getRender(FAST_BLUR);
        if (render == null) {
            render = new ProgressBlurRender(canvas);
            canvas.addRender(render);
        }
        return (ProgressBlurRender)render;
    }

    @Override
    public BlurResult blur2Target(Texture input, int bitmapWidth, int bitmapHeight) {
        if (skipBlur()) return null;
        float scale = mDrawingParameters.getScale();
        int radius = (int)(mDrawingParameters.getRadius()*mDrawingParameters.getLevel() + 0.5f);
        int passCount = Math.min(mDrawingParameters.getPassCount(), 3);
        int width = Math.max((int)(bitmapWidth*scale), 1);
        int height = Math.max((int)(bitmapHeight*scale), 1);
        Texture tmpTexture = GLRenderer.getTexturePool().get(width, height, TEXTURE_FORMAT, true);
        Texture outputTexture = GLRenderer.getTexturePool().get(width, height, TEXTURE_FORMAT, true);
        tmpTexture.onBind(mGLCanvas);
        outputTexture.onBind(mGLCanvas);
        boxesForGauss(Math.max(radius, 0), passCount);
        for (int i=0; i<passCount; ++i) {
            if (i == 0) {
                blurH(input, tmpTexture, (mSize[i] - 1) / 2);
            } else {
                GLES20.glUseProgram(mProgramH);
                GLES31Utils.glBindImageTexture(0, outputTexture.getId(), 0, false, 0, GLES31Utils.GL_READ_ONLY, TEXTURE_FORMAT);
                GLES31Utils.glBindImageTexture(1, tmpTexture.getId(), 0, false, 0, GLES31Utils.GL_WRITE_ONLY, TEXTURE_FORMAT);
                GLES20.glUniform1i(mUniformRadiusH, (mSize[i] - 1) / 2);
                GLES31Utils.glDispatchCompute(1, RendererUtils.nextMultipleN(height, BLUR_WORK_SIZE)/BLUR_WORK_SIZE, 1);
                GLES31Utils.glMemoryBarrier(GLES31Utils.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
            }
            if (mNotSupportMemoryBarrier) GLES20.glFinish();
            GLES20.glUseProgram(mProgramV);
            GLES31Utils.glBindImageTexture(0, tmpTexture.getId(), 0, false, 0, GLES31Utils.GL_READ_ONLY, TEXTURE_FORMAT);
            GLES31Utils.glBindImageTexture(1, outputTexture.getId(), 0, false, 0, GLES31Utils.GL_WRITE_ONLY, TEXTURE_FORMAT);
            GLES20.glUniform1i(mUniformRadiusV, (mSize[i] - 1) / 2);
            GLES31Utils.glDispatchCompute(RendererUtils.nextMultipleN(width, BLUR_WORK_SIZE)/BLUR_WORK_SIZE, 1, 1);
            GLES31Utils.glMemoryBarrier(GLES31Utils.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        }
        GLRenderer.getTexturePool().put(tmpTexture, true);
        return BlurResult.newInstance(outputTexture);
    }

    private void blurH(Texture input, Texture output, int radius) {
        GLES20.glUseProgram(mProgramS);
        GLES31Utils.glMemoryBarrier(GLES31Utils.GL_TEXTURE_UPDATE_BARRIER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, input.getId());
        GLES20.glUniform1i(mUniformTextureS, 0);
        GLES20.glUniform1i(mUniformRadiusS, radius);
        GLES31Utils.glBindImageTexture(0, output.getId(), 0, false, 0, GLES31Utils.GL_WRITE_ONLY, TEXTURE_FORMAT);
        GLES31Utils.glDispatchCompute(1, RendererUtils.nextMultipleN(output.getHeight(), BLUR_WORK_SIZE)/BLUR_WORK_SIZE, 1);
        GLES31Utils.glMemoryBarrier(GLES31Utils.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
    }

    private void boxesForGauss(float radius, int n) {
        if (mSize.length != n) {
            mSize = new int[n];
        }
        float sigma = radius;
        float wIdeal = (float)Math.sqrt(12.0 * sigma * sigma / n + 1.0);
        int wl = (int)Math.floor(wIdeal);
        if (0 == wl % 2) wl--;
        int wu = wl + 2;
        float mIdeal = (12.0f * sigma * sigma - n * wl * wl - 4 * n * wl - 3 * n) / (-4 * wl - 4);
        int m = Math.round(mIdeal);
        for (int i = 0; i < n; ++i) mSize[i] = (i < m ? wl : wu);
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        super.trimResources(level, hasEglContext);
        if (mGLCanvas != null) {
            mGLCanvas.deleteProgram(mProgramH, hasEglContext);
            mProgramH = 0;
            mGLCanvas.deleteProgram(mProgramV, hasEglContext);
            mProgramV = 0;
            mGLCanvas.deleteProgram(mProgramS, hasEglContext);
            mGLCanvas = null;
        }
    }

    public static void glTexStorage2D(int target,int levels,int internalformat,int width,int height) {
        GLES31Utils.glTexStorage2D(target, levels, internalformat, width, height);
    }

    private static String getPixelFormat() {
       return (TEXTURE_FORMAT == GLES31Utils.GL_RGBA32F) ? "rgba32f" : "rgba16f";
    }

    private String getBlurShader(boolean vertical) {
        String shader =
            "#version 310 es \n" +
            "precision highp float; \n" +
            "uniform int uRadius; \n" +
            "layout("+getPixelFormat()+", binding = 0) readonly uniform highp image2D uInputImage; \n" +
            "layout("+getPixelFormat()+", binding = 1) writeonly uniform highp image2D uOutputImage; \n" +
            "layout (local_size_x = "+(vertical ? BLUR_WORK_SIZE : 1)+", local_size_y = "+(vertical ? 1 : BLUR_WORK_SIZE )+", local_size_z = 1) in;\n" +
            "ivec2 offset(int value, int base) {\n" +
            "    return "+vertical+" ? ivec2(base, value) : ivec2(value, base);\n" +
            "}\n"+

            "void boxBlur() {\n" +
            "    int size = "+vertical+" ? int(imageSize(uOutputImage).y) : int(imageSize(uOutputImage).x);\n" +
            "    int base = "+vertical+" ? int(gl_GlobalInvocationID.x) : int(gl_GlobalInvocationID.y);\n" +
            "    int radius = min(uRadius, size);\n"+
            "    float weight = 1.0/float(radius*2+1);\n" +
            "    vec3 left  = imageLoad(uInputImage, offset(0, base)).rgb;\n" +
            "    vec3 right = imageLoad(uInputImage, offset(size-1, base)).rgb;\n" +
            "    vec3 color = left*float(radius+1);\n" +
            "    for(int i=0; i<radius; i++) {\n" +
            "        color += imageLoad(uInputImage, offset(i, base)).rgb;\n" +
            "    }\n" +

            "    for(int i=0; i<=radius; i++) { \n" +
            "        color += imageLoad(uInputImage, offset(i+radius, base)).rgb - left;\n" +
            "        imageStore(uOutputImage, offset(i, base), vec4(color*weight, 1.0));\n" +
            "    }\n" +

            "    for(int i=radius+1; i<size-radius; i++) { \n" +
            "        color += imageLoad(uInputImage, offset(i+radius, base)).rgb - \n" +
            "                 imageLoad(uInputImage, offset(i-radius-1, base)).rgb;\n" +
            "        imageStore(uOutputImage, offset(i, base), vec4(color*weight, 1.0));\n" +
            "    }\n" +

            "    for(int i=size-radius; i<size; i++) { \n" +
            "        color += right - imageLoad(uInputImage, offset(i-radius-1, base)).rgb;\n" +
            "        imageStore(uOutputImage, offset(i, base), vec4(color*weight, 1.0));\n" +
            "    }\n" +
            "}\n"+

//            "void boxBlur2() {\n" +
//            "    int size = "+vertical+" ? int(imageSize(uOutputImage).y) : int(imageSize(uOutputImage).x);\n" +
//            "    int base =  "+vertical+" ? int(gl_GlobalInvocationID.x) : int(gl_GlobalInvocationID.y);\n" +
//            "    int radius = min(uRadius, size);\n"+
//            "    float weight = 1.0/float(radius*2+1);\n" +
//            "    vec3 color = imageLoad(uInputImage, offset(0, base)).rgb*float(radius+1);\n" +
//            "    for(int i=0; i<radius; i++) {\n" +
//            "        color += imageLoad(uInputImage, offset(i, base)).rgb;\n" +
//            "    }\n" +
//
//            "    for(int i=0; i<size; i++) { \n" +
//            "        color += imageLoad(uInputImage, offset(min(i+radius,size-1), base)).rgb - \n" +
//            "                 imageLoad(uInputImage, offset(max(i-radius-1,0), base)).rgb;\n" +
//            "        imageStore(uOutputImage, offset(i, base), vec4(color*weight, 1.0));\n" +
//            "    }\n" +
//            "}\n"+

            "void main() { \n" +
            "    boxBlur();\n" +
            "}";
        return shader;
    }


    private static String scaleShader =
            "#version 310 es \n" +
            "precision highp float; \n" +
            "uniform sampler2D uInputImage; \n" +
            "uniform int uRadius; \n" +
            "layout("+getPixelFormat()+", binding = 0) writeonly uniform highp image2D uOutputImage; \n" +
            "layout (local_size_x = 1, local_size_y = "+BLUR_WORK_SIZE +", local_size_z = 1) in;\n" +
            "ivec2 offset(int value, int base) {\n" +
            "    return ivec2(value, base);\n" +
            "}\n"+
            "vec3 imageFetch(ivec2 pos, ivec2 imageSize) {\n" +
            "     return texture(uInputImage, vec2(float(pos.x)/float(imageSize.x), float(pos.y)/float(imageSize.y))).rgb;" +
            "}\n"+

            "void boxBlurH() {\n" +
            "    ivec2 imageSize = ivec2(imageSize(uOutputImage));\n" +
            "    int size =  int(imageSize.x);\n" +
            "    int base =  int(gl_GlobalInvocationID.y);\n" +
            "    int radius = min(uRadius, size);\n"+
            "    float weight = 1.0/float(radius*2+1);\n" +
            "    vec3 left  = imageFetch(offset(0, base), imageSize);\n" +
            "    vec3 right = imageFetch(offset(size-1, base), imageSize);\n" +
            "    vec3 color = left*float(radius+1);\n" +
            "    for(int i=0; i<radius; i++) {\n" +
            "        color += imageFetch(offset(i, base), imageSize);\n" +
            "    }\n" +

            "    for(int i=0; i<=radius; i++) { \n" +
            "        color += imageFetch(offset(i+radius, base), imageSize) - left;\n" +
            "        imageStore(uOutputImage, offset(i, base), vec4(color*weight, 1.0));\n" +
            "    }\n" +

            "    for(int i=radius+1; i<size-radius; i++) { \n" +
            "        color += imageFetch(offset(i+radius, base), imageSize) - \n" +
            "                 imageFetch(offset(i-radius-1, base),imageSize);\n" +
            "        imageStore(uOutputImage, offset(i, base), vec4(color*weight, 1.0));\n" +
            "    }\n" +

            "    for(int i=size-radius; i<size; i++) { \n" +
            "        color += right - imageFetch(offset(i-radius-1, base), imageSize);\n" +
            "        imageStore(uOutputImage, offset(i, base), vec4(color*weight, 1.0));\n" +
            "    }\n" +
            "}\n"+

            "void main() { \n" +
            "    boxBlurH();\n" +
            "}";

//    private static final String scaleShader =
//            "#version 310 es \n" +
//            "precision highp float; \n" +
//            "uniform sampler2D sTexture;\n"+
//            "layout("+getPixelFormat()+", binding = 0) writeonly uniform highp image2D uOutputImage; \n" +
//            "layout (local_size_x = 8, local_size_y = 16, local_size_z = 1) in;\n" +
//
//            "void main() { \n" +
//            "    ivec2 pos = ivec2(gl_GlobalInvocationID);\n" +
//            "    ivec2 imageSize = ivec2(imageSize(uOutputImage));\n" +
//            "    imageStore(uOutputImage, pos, " +
//            "       texture(sTexture, vec2(float(pos.x)/float(imageSize.x), float(pos.y)/float(imageSize.y)))); \n" +
//            "}";
}
