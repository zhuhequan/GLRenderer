/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.ShaderUtils;
import com.android.common.renderer.effect.op.DrawTextureOp;

import java.nio.FloatBuffer;


public abstract class PixelsRender extends ShaderRender {
    private static final float[] VERTICES = {0,0,0,  1,0,0,  0,1,0,  1,1,0};
    private static final float[] TEXTURES = {0,0,  1,0,  0,1,  1,1};
    private static FloatBuffer VERTICES_BUFFER;
    private static FloatBuffer TEXTURES_BUFFER;

    public PixelsRender(GLCanvas canvas) {
        super(canvas);
    }

    @Override
    protected void initProgram() {
        mProgram = ShaderUtils.createProgram(getVertexShader(), getFragmentShader());
        if (mProgram != 0) {
            GLES20.glUseProgram(mProgram);
            mUniformMVPMatrixH = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            mUniformSTMatrixH = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
            mUniformAlphaH = GLES20.glGetUniformLocation(mProgram, "uAlpha");
            mUniformTextureH = GLES20.glGetUniformLocation(mProgram, "sTexture");
            mAttributePositionH = GLES20.glGetAttribLocation(mProgram, "aPosition");
            mAttributeTexCoorH = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        } else {
            throw new IllegalArgumentException(getClass() + ": mProgram = 0");
        }
    }

    @Override
    public boolean draw(DrawInfo drawInfo) {
        drawTexture(drawInfo, (DrawTextureOp) drawInfo.drawOp);
        return true;
    }

    @Override
    protected void initShader(DrawInfo drawInfo) {
        GLES20.glVertexAttribPointer(mAttributePositionH,
                                     3,
                                     GLES20.GL_FLOAT,
                                     false,
                                     3*4,
                                     getVertexBuffer());

        GLES20.glVertexAttribPointer(mAttributeTexCoorH,
                                     2,
                                     GLES20.GL_FLOAT,
                                     false,
                                     2*4,
                                     getTextureBuffer());

        GLES20.glUniformMatrix4fv(mUniformMVPMatrixH,
                                  1,
                                  false,
                                  mGLCanvas.getState().getFinalMatrix(),
                                  0);

        GLES20.glUniformMatrix4fv(mUniformSTMatrixH,
                                  1,
                                  false,
                                  mGLCanvas.getState().getTexMaxtrix(),
                                  0);
        GLES20.glUniform1i(mUniformTextureH, 0);
        GLES20.glUniform1f(mUniformAlphaH, (float)drawInfo.alpha/255);
        GLES20.glEnableVertexAttribArray(mAttributePositionH);
        GLES20.glEnableVertexAttribArray(mAttributeTexCoorH);
    }

    protected void drawTexture(DrawInfo drawInfo, DrawTextureOp drawOp) {
        GLES20.glUseProgram(mProgram);

        if (!drawOp.texture.onBind(mGLCanvas)) return;

        bindTexture(drawOp.texture, GLES20.GL_TEXTURE0);

        onPreDraw(drawInfo);

        drawOp.texture.updateTransformMatrix(mGLCanvas, drawInfo.flipTextureH, drawInfo.flipTextureV);

        if (drawOp.x != 0 || drawOp.y != 0) {
            mGLCanvas.getState().translate(drawOp.x, drawOp.y, 0);
        }
        mGLCanvas.getState().scale(drawOp.width, drawOp.height, 1);
        initShader(drawInfo);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        onPostDraw(drawInfo);
    }

    protected FloatBuffer getVertexBuffer() {
        return VERTICES_BUFFER;
    }

    protected FloatBuffer getTextureBuffer() {
        return TEXTURES_BUFFER;
    }

    private static void init() {
        VERTICES_BUFFER = allocateByteBuffer(VERTICES.length*Float.SIZE / Byte.SIZE).asFloatBuffer();
        VERTICES_BUFFER.put(VERTICES);
        VERTICES_BUFFER.position(0);

        TEXTURES_BUFFER = allocateByteBuffer(TEXTURES.length*Float.SIZE / Byte.SIZE).asFloatBuffer();
        TEXTURES_BUFFER.put(TEXTURES);
        TEXTURES_BUFFER.position(0);
    }

    static {
        init();
    }
}
