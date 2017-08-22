/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.op.DrawTextureOp;
public class GaussianRender extends ConvolutionRender {
    public static final String KEY = "__gaussian";
    public static final int MAGIC_RADIUS = 123;
    protected int mUniformRadius;
    protected int mUniformWeight;
    protected int mUniformVertical;
    private boolean mVertical;
    private int mRadius;
    private float mWeight;


    public static GaussianRender getInstace(GLCanvas canvas) {
        Render render = canvas.getRender(GaussianRender.KEY);
        if (render == null) {
            render = new GaussianRender(canvas);
            canvas.addRender(render);
        }
        return (GaussianRender)render;
    }

    public GaussianRender(GLCanvas canvas) {
        super(canvas);
        mKey = KEY;
        mStepX = 1.0f;
        mStepY = 1.0f;
        setRadius(4);
    }

    @Override
    protected void initProgram() {
        super.initProgram();
        mUniformVertical = GLES20.glGetUniformLocation(mProgram, "uVertical");
        mUniformRadius = GLES20.glGetUniformLocation(mProgram, "uRadius");
        mUniformWeight = GLES20.glGetUniformLocation(mProgram, "uWeight");
    }

    public void setDirection(boolean vertical) {
        mVertical = vertical;
    }

    @Override
    public String getFragmentShader() {
        return FRAG;
    }

    public void setRadius(int radius) {
        mRadius = radius;
        mWeight = 1f/(2*radius+1);
    }

    @Override
    protected void initShader(DrawInfo drawInfo) {
        super.initShader(drawInfo);
        DrawTextureOp element = (DrawTextureOp)(drawInfo.drawOp);

        mStepX = 1f/element.width;
        mStepY = 1f/element.height;

        if (!mVertical) {
            GLES20.glUniform2f(mUniformStepH, mStepX, 0);
            GLES20.glUniform1i(mUniformVertical, 0);
        } else {
            GLES20.glUniform2f(mUniformStepH, 0, mStepY);
            GLES20.glUniform1i(mUniformVertical, 1);
        }
        GLES20.glUniform1f(mUniformWeight, mWeight);
        GLES20.glUniform1i(mUniformRadius, mRadius);
    }

    private static final String FRAG =
            "precision mediump float; \n" +
            "uniform vec2 uStep; \n" +
            "uniform sampler2D sTexture; \n" +
            "varying vec2 vTexCoord; \n" +
            "uniform int uVertical; \n" +
            "uniform int uRadius; \n" +
            "uniform float uWeight; \n" +

            "vec4 gassian(vec2 step) { \n" +
            "    if (uRadius == 0) return texture2D(sTexture, vTexCoord); \n" +
            "    vec3 sum; \n" +
            "    float j=0.0;  \n" +
            "    for (int i=0; i<=uRadius; ++i) {  \n" +
            "        if (i == 0) { \n" +
            "           sum = texture2D(sTexture, vTexCoord).rgb * uWeight; \n" +
            "        } else {  \n" +
            "           sum += texture2D(sTexture,uVertical==1 ? vec2(vTexCoord.x,vTexCoord.y-j*step.y) : vec2(vTexCoord.x-j*step.x,vTexCoord.y)).rgb * uWeight;\n" +
            "           sum += texture2D(sTexture,uVertical==1 ? vec2(vTexCoord.x,vTexCoord.y+j*step.y) : vec2(vTexCoord.x+j*step.x,vTexCoord.y)).rgb * uWeight;\n" +
            "        }\n" +
            "        j += 1.0;\n" +
            "    }\n"+
            "    return vec4(sum, 1.0); \n" +
            "} \n" +

            "vec4 gassian2(vec2 step) { \n" +
            "    vec3 sum; \n" +
            "	 float a[6]; \n" +
            "    a[0] = 0.001; a[1] = 0.01; a[2] = 0.044; a[3] = 0.116; a[4] = 0.205; a[5] = 0.246; \n" +
            "    //a[0] = 0.0355; a[1] = 0.0585; a[2] = 0.0863; a[3] = 0.1139; a[4] = 0.1346; a[5] = 0.1423; \n" +
            "    //a[0] = 0.0549; a[1] = 0.0727; a[2] = 0.0905; a[3] = 0.1058; a[4] = 0.1162; a[5] = 0.1199; \n" +
            "    sum  = texture2D(sTexture, vTexCoord - 5.0 * step).rgb * a[0]; \n" +
            "    sum += texture2D(sTexture, vTexCoord - 4.0 * step).rgb * a[1]; \n" +
            "    sum += texture2D(sTexture, vTexCoord - 3.0 * step).rgb * a[2]; \n" +
            "    sum += texture2D(sTexture, vTexCoord - 2.0 * step).rgb * a[3]; \n" +
            "    sum += texture2D(sTexture, vTexCoord - step).rgb * a[4]; \n" +
            "    sum += texture2D(sTexture, vTexCoord).rgb * a[5]; \n" +
            "    sum += texture2D(sTexture, vTexCoord + step).rgb * a[4]; \n" +
            "    sum += texture2D(sTexture, vTexCoord + 2.0 * step).rgb * a[3]; \n" +
            "    sum += texture2D(sTexture, vTexCoord + 3.0 * step).rgb * a[2]; \n" +
            "    sum += texture2D(sTexture, vTexCoord + 4.0 * step).rgb * a[1]; \n" +
            "    sum += texture2D(sTexture, vTexCoord + 5.0 * step).rgb * a[0]; \n" +
            "    return vec4(sum, 1.0); \n" +
            "} \n" +

            "void main() { \n" +
            "    if (uRadius <= 100) { \n" +
            "        gl_FragColor = gassian(uStep); \n" +
            "    } else { \n"+
            "        gl_FragColor = gassian2(uStep);\n" +
            "    } \n" +
            "} \n";
 }
