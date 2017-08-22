/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.op.DrawTextureOp;

import java.nio.FloatBuffer;
public abstract class MeshRender extends PixelsRender {
    private final float UNIT_SIZE = 2.0f;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private int mVertexCount;
    private int mCols = 12;
    private int mRows = 12;
    public MeshRender(GLCanvas canvas) {
        super(canvas);
        initMesh();
    }

    public void setGrid(int cols,int rows) {
        if (mCols != cols || mRows != rows) {
            mCols = cols;
            mRows = rows;
            initMesh();
        }
    }

    @Override
    protected void drawTexture(DrawInfo drawInfo, DrawTextureOp drawOp) {
        GLES20.glUseProgram(mProgram);

        if (!drawOp.texture.onBind(mGLCanvas)) return;

        bindTexture(drawOp.texture, GLES20.GL_TEXTURE0);

        onPreDraw(drawInfo);
        drawOp.texture.updateTransformMatrix(mGLCanvas, drawInfo.flipTextureH, drawInfo.flipTextureV);
        initShader(drawInfo);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);

        onPostDraw(drawInfo);
    }

    @Override
    protected void updateViewport(DrawInfo drawInfo) {
        int width = drawInfo.viewportWidth;
        int height = drawInfo.viewportHeight;
        float scale = 4.0f;
        GLES20.glViewport(0, 0, width, height);
        if (drawInfo.flipProjV) {
            mGLCanvas.getState().frustumM(-1/scale, 1/scale, 1/scale, -1/scale, 1, 100);
        } else {
            mGLCanvas.getState().frustumM(-1/scale, 1/scale, -1/scale, 1/scale, 1, 100);
        }
        mGLCanvas.getState().setLookAt(0f, 0f, scale,
                                       0f, 0f, 0f,
                                       0f, 1f, 0f);
    }


    @Override
    protected FloatBuffer getVertexBuffer() {
        return mVertexBuffer;
    }

    @Override
    protected FloatBuffer getTextureBuffer() {
        return mTextureBuffer;
    }

    private void initMesh() {
        final float stepX = UNIT_SIZE/mCols;
        final float stepY = UNIT_SIZE/mRows;
        mVertexCount = mCols*mRows*6;
        float vertices[] = new float[mVertexCount*3];
        int count=0;

        /** init vertex*/
        for (int j=0; j<mRows; j++) {
            for (int i=0; i<mCols; i++) {
                float zsx = -UNIT_SIZE/2 + i*stepX;
                float zsy = -UNIT_SIZE/2 + j*stepY;
                float zsz = 0;

                vertices[count++] = zsx;
                vertices[count++] = zsy;
                vertices[count++] = zsz;

                vertices[count++] = zsx;
                vertices[count++] = zsy + stepY;
                vertices[count++] = zsz;

                vertices[count++] = zsx + stepX;
                vertices[count++] = zsy;
                vertices[count++] = zsz;

                vertices[count++] = zsx + stepX;
                vertices[count++] = zsy;
                vertices[count++] = zsz;

                vertices[count++] = zsx;
                vertices[count++] = zsy + stepY;
                vertices[count++] = zsz;

                vertices[count++] = zsx + stepX;
                vertices[count++] = zsy + stepY;
                vertices[count++] = zsz;
            }
        }

        /**init texcoord*/
        float[] texcoords=new float[mVertexCount*2];
        float sizew=1.0f/mCols;
        float sizeh=1.0f/mRows;
        int c=0;
        for (int i = 0; i < mRows; i++) {
            for (int j = 0; j < mCols; j++) {
                float s = j * sizew;
                float t = i * sizeh;

                texcoords[c++] = s;
                texcoords[c++] = t;

                texcoords[c++] = s;
                texcoords[c++] = t + sizeh;

                texcoords[c++] = s + sizew;
                texcoords[c++] = t;

                texcoords[c++] = s + sizew;
                texcoords[c++] = t;

                texcoords[c++] = s;
                texcoords[c++] = t + sizeh;

                texcoords[c++] = s + sizew;
                texcoords[c++] = t + sizeh;
            }
        }

        mVertexBuffer = allocateByteBuffer(vertices.length*4).asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        mTextureBuffer = allocateByteBuffer(texcoords.length*4).asFloatBuffer();
        mTextureBuffer.put(texcoords);
        mTextureBuffer.position(0);
    }

}
