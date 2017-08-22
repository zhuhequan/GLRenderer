package com.android.glede;

import android.opengl.GLES20;

import com.android.common.renderer.effect.ArrayBuffer;
import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.EffectUtils;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.ShaderUtils;
import com.android.common.renderer.effect.op.DrawTextureOp;
import com.android.common.renderer.effect.render.ShaderRender;
import com.android.common.renderer.effect.texture.Texture;

import java.nio.FloatBuffer;

public class GledeRender extends ShaderRender {
    private int mAttributePosition2H;
    private int mAttributePosition3H;
    private int mUniformFactorH;

    private ArrayBuffer mVertexBuffer1;
    private ArrayBuffer mVertexBuffer2;
    private ArrayBuffer mVertexBuffer3;
    private ArrayBuffer mTexCoordBuffer;
    private int mVertexCount;
    private float mRatio = 0.0f;
    public static final String KEY_GLEDE = "1";

    public GledeRender(GLCanvas canvas) {
        super(canvas);
        mKey = KEY_GLEDE;
        initVertexData();
    }

    public void setRatio(float ratio) {
        mRatio = ratio;
    }

    @Override
    protected void initProgram() {
        mProgram = ShaderUtils.createProgram(getVertexShader(), getFragmentShader());
        if (mProgram != 0) {
            GLES20.glUseProgram(mProgram);
            mUniformMVPMatrixH = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            mUniformTextureH = GLES20.glGetUniformLocation(mProgram, "sTexture");
            mUniformFactorH = GLES20.glGetUniformLocation(mProgram, "uFactor");

            mAttributePositionH = GLES20.glGetAttribLocation(mProgram, "aPosition");
            mAttributePosition2H = GLES20.glGetAttribLocation(mProgram, "bPosition");
            mAttributePosition3H = GLES20.glGetAttribLocation(mProgram, "cPosition");
            mAttributeTexCoorH = GLES20.glGetAttribLocation(mProgram, "aTexCoord");

        } else {
            throw new IllegalArgumentException(getClass() + ": mProgram = 0");
        }
    }

    private void initVertexData() {
        float glede1[][] = EffectUtils.loadFromFileVertexOnly("glede_0.obj");
        float glede2[] = EffectUtils.loadFromFileVertexOnly("glede_1.obj")[0];
        float glede3[] = EffectUtils.loadFromFileVertexOnly("glede_2.obj")[0];

        mVertexCount = glede1[0].length / 3;

        FloatBuffer buffer = allocateByteBuffer(glede1[0].length * 4).asFloatBuffer();
        buffer.put(glede1[0]);
        buffer.position(0);
        mVertexBuffer1 = new ArrayBuffer(mGLCanvas, buffer);

        buffer = allocateByteBuffer(glede2.length * 4).asFloatBuffer();
        buffer.put(glede2);
        buffer.position(0);
        mVertexBuffer2 = new ArrayBuffer(mGLCanvas, buffer);

        buffer = allocateByteBuffer(glede3.length * 4).asFloatBuffer();
        buffer.put(glede3);
        buffer.position(0);
        mVertexBuffer3 = new ArrayBuffer(mGLCanvas, buffer);

        buffer = allocateByteBuffer(glede1[1].length * 4).asFloatBuffer();
        buffer.put(glede1[1]);
        buffer.position(0);
        mTexCoordBuffer = new ArrayBuffer(mGLCanvas, buffer);
    }

    @Override
    protected void initShader(DrawInfo renderInfo) {
        mVertexBuffer1.bindSelf();
        GLES20.glVertexAttribPointer(mAttributePositionH,
                3,
                GLES20.GL_FLOAT,
                false,
                0,
                0);

        mVertexBuffer2.bindSelf();
        GLES20.glVertexAttribPointer(mAttributePosition2H,
                3,
                GLES20.GL_FLOAT,
                false,
                0,
                0);

        mVertexBuffer3.bindSelf();
        GLES20.glVertexAttribPointer(mAttributePosition3H, 3,
                GLES20.GL_FLOAT,
                false,
                0,
                0);

        mTexCoordBuffer.bindSelf();
        GLES20.glVertexAttribPointer(mAttributeTexCoorH,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                0);


        GLES20.glUniformMatrix4fv(mUniformMVPMatrixH,
                1,
                false,
                mGLCanvas.getState().getFinalMatrix(),
                0);
        GLES20.glUniform1i(mUniformTextureH, 0);
        GLES20.glUniform1f(mUniformFactorH, mRatio);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glEnableVertexAttribArray(mAttributePositionH);
        GLES20.glEnableVertexAttribArray(mAttributePosition2H);
        GLES20.glEnableVertexAttribArray(mAttributePosition3H);
        GLES20.glEnableVertexAttribArray(mAttributeTexCoorH);
    }


    @Override
    public boolean draw(DrawInfo renderInfo) {
        Texture texture = ((DrawTextureOp)renderInfo.drawOp).texture;
        GLES20.glUseProgram(mProgram);

        if (!texture.onBind(mGLCanvas)) return false;
        onPreDraw(renderInfo);
        bindTexture(texture, GLES20.GL_TEXTURE0);
        initShader(renderInfo);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);
        onPostDraw(renderInfo);

        return true;
    }

    @Override
    protected String getVertexShader() {
        return ShaderUtils.loadFromAssetsFile("vertex_glede.txt");
    }

    @Override
    protected String getFragmentShader() {
        return ShaderUtils.loadFromAssetsFile("frag_glede.txt");
    }

    @Override
    protected void updateViewport(DrawInfo renderInfo) {
        int width = renderInfo.viewportWidth;
        int height = renderInfo.viewportHeight;
        float ratio = (float) width / height;
        GLES20.glViewport(0, 0, width, height);
        if (renderInfo.flipProjV) {
            mGLCanvas.getState().frustumM(-ratio, ratio, 1, -1, 2, 1000);
        } else {
            mGLCanvas.getState().frustumM(-ratio, ratio, -1, 1, 2, 1000);
        }
        mGLCanvas.getState().setLookAt(0, 0, 0, 0f, 0f, -1f, 0f, 1.0f, 0.0f);
    }


    @Override
    public void trimResources(int level, boolean hasEglContext) {
        super.trimResources(level, hasEglContext);
        if (mVertexBuffer1 != null) {
            mVertexBuffer1.releaseResources(hasEglContext);
            mVertexBuffer1 = null;
        }
        if (mVertexBuffer2 != null) {
            mVertexBuffer2.releaseResources(hasEglContext);
            mVertexBuffer2 = null;
        }
        if (mVertexBuffer3 != null) {
            mVertexBuffer3.releaseResources(hasEglContext);
            mVertexBuffer3 = null;
        }
        if (mTexCoordBuffer != null) {
            mTexCoordBuffer.releaseResources(hasEglContext);
            mTexCoordBuffer = null;
        }
    }

}
