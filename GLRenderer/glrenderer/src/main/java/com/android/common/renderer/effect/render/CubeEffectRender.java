/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import java.nio.FloatBuffer;

import android.opengl.GLES20;

import com.android.common.renderer.effect.FrameBuffer;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.op.DrawOp;
import com.android.common.renderer.effect.op.DrawTextureOp;
public class CubeEffectRender extends Render {
    public static final String  CUBE ="__cube";
    private int mAngleX;
    private int mAngleY;
    private int mAngleZ;
    private CubeRender mCubeRender;
    private DrawTextureOp mTextureElement = new DrawTextureOp();
    private DrawInfo mEffectInfo = new DrawInfo();
    public CubeEffectRender(GLCanvas canvas) {
        super(canvas);
        mKey = CUBE;
        mCubeRender = new CubeRender(canvas);
    }

    public void drawTopottom(boolean draw) {
        mCubeRender.drawTopottom(draw);
    }

    public void setAngleX(int angle) {
        mAngleX = angle;
    }

    public void setAngleY(int angle) {
        mAngleY = angle;
    }

    public void setAngleZ(int angle) {
        mAngleZ = angle;
    }

    @Override
    public boolean draw(DrawInfo drawInfo) {
        switch (drawInfo.drawOp.getId()) {

        case DrawOp.TEXTURE:
            drawTexure(drawInfo);
            return true;
        }
        return false;
    }

    private void drawTexure(DrawInfo drawInfo) {
        DrawTextureOp element = (DrawTextureOp)drawInfo.drawOp;
        int width = element.width;
        int height = element.height;
        FrameBuffer framebuffer = GLRenderer.getFrameBufferPool().get(width, height, true, true);
        framebuffer.onBind(mGLCanvas);

        mTextureElement.init(element.texture, 0, 0, width, height);
        mEffectInfo.clearFbo = true;
        mEffectInfo.cullFace = true;
        mEffectInfo.depthTest = true;
        mEffectInfo.viewportWidth = width;
        mEffectInfo.viewportHeight = height;
        mEffectInfo.drawOp = mTextureElement;

        mGLCanvas.getState().push();
        mGLCanvas.getState().identityModelM();
        mGLCanvas.getState().identityTexM();
        if (mAngleX != 0) {
            mGLCanvas.getState().rotate(mAngleX, 1, 0, 0);
        }
        if (mAngleY != 0) {
            mGLCanvas.getState().rotate(mAngleY, 0, 1, 0);
        }
        if (mAngleZ != 0) {
            mGLCanvas.getState().rotate(mAngleZ, 0, 0, 1);
        }

        mGLCanvas.getState().setFrameBufferId(framebuffer.getId());

        mCubeRender.draw(mEffectInfo);
        mGLCanvas.getState().pop();

        mTextureElement.texture = null;
        mEffectInfo.reset();

        element.texture = framebuffer.getTexture();
        mGLCanvas.getRender(drawInfo.effectKey).draw(drawInfo);
        GLRenderer.getFrameBufferPool().put(framebuffer, true);
    }



    @Override
    public void trimResources(int level, boolean hasEglContext) {
        mCubeRender.trimResources(level, hasEglContext);
        super.trimResources(level, hasEglContext);
    }

    private class CubeRender extends PixelsRender {
        private static final float UNIT_SIZE = 2.0f;
        private FloatBuffer mVertexBuffer;
        private FloatBuffer mTextureBuffer;
        private boolean mDrawTopBottom = true;
        public CubeRender(GLCanvas canvas) {
            super(canvas);
            initMesh();
        }

        public void drawTopottom(boolean draw) {
            mDrawTopBottom = draw;
        }

        @Override
        public boolean draw(DrawInfo drawInfo) {
            switch (drawInfo.drawOp.getId()) {

            case DrawOp.TEXTURE: {
                DrawTextureOp element = (DrawTextureOp) drawInfo.drawOp;
                onPreDraw(drawInfo);
                if (!element.texture.onBind(mGLCanvas)) return false;
                bindTexture(element.texture, GLES20.GL_TEXTURE0);
                element.texture.updateTransformMatrix(mGLCanvas, drawInfo.flipTextureH, drawInfo.flipTextureV);
                final float tzz=0.01f;
                //前面
                mGLCanvas.getState().push();
                mGLCanvas.getState().translate(0, 0, UNIT_SIZE/2-tzz);
                drawSelf(drawInfo);
                mGLCanvas.getState().pop();

                //后面
                mGLCanvas.getState().push();
                mGLCanvas.getState().translate(0, 0, -UNIT_SIZE/2+tzz);
                mGLCanvas.getState().rotate(180, 0, 1, 0);
                drawSelf(drawInfo);
                mGLCanvas.getState().pop();

                //左面
                mGLCanvas.getState().push();
                mGLCanvas.getState().translate(-UNIT_SIZE/2+tzz, 0, 0);
                mGLCanvas.getState().rotate(-90, 0, 1, 0);
                drawSelf(drawInfo);
                mGLCanvas.getState().pop();

                //右面
                mGLCanvas.getState().push();
                mGLCanvas.getState().translate(UNIT_SIZE/2-tzz, 0, 0);
                mGLCanvas.getState().rotate(90, 0, 1, 0);
                drawSelf(drawInfo);
                mGLCanvas.getState().pop();

                if (mDrawTopBottom) {
                    //上面
                    mGLCanvas.getState().push();
                    mGLCanvas.getState().translate(0, UNIT_SIZE/2-tzz, 0);
                    mGLCanvas.getState().rotate(-90, 1, 0, 0);
                    drawSelf(drawInfo);
                    mGLCanvas.getState().pop();

                    //下面
                    mGLCanvas.getState().push();
                    mGLCanvas.getState().translate(0, -UNIT_SIZE/2+tzz, 0);
                    mGLCanvas.getState().rotate(90, 1, 0, 0);
                    drawSelf(drawInfo);
                    mGLCanvas.getState().pop();
                }
                onPostDraw(drawInfo);
                return true;
            }
            }
            return false;
        }

        @Override
        protected FloatBuffer getVertexBuffer() {
            return mVertexBuffer;
        }

        @Override
        protected FloatBuffer getTextureBuffer() {
            return mTextureBuffer;
        }

        protected void drawSelf(DrawInfo drawInfo) {
            GLES20.glUseProgram(mProgram);
            initShader(drawInfo);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        }

        @Override
        protected void updateViewport(DrawInfo drawInfo) {
            int width = drawInfo.viewportWidth;
            int height = drawInfo.viewportHeight;
            float scale = 4.0f;
            GLES20.glViewport(0, 0, width, height);
            mGLCanvas.getState().frustumM(-1/scale, 1/scale, -1/scale, 1/scale, 1, 100);
            mGLCanvas.getState().setLookAt(0, 0f, scale+UNIT_SIZE/2,
                    0f, 0f,0f,
                    0f, 1f, 0f);
        }

        private void initMesh() {
            float vertices[] = new float[6*3];
            int count=0;

            float zsx = -UNIT_SIZE/2;
            float zsy = -UNIT_SIZE/2;
            float zsz = 0;
            vertices[count++] = zsx;
            vertices[count++] = zsy;
            vertices[count++] = zsz;

            vertices[count++] = zsx + UNIT_SIZE;;
            vertices[count++] = zsy;
            vertices[count++] = zsz;

            vertices[count++] = zsx;
            vertices[count++] = zsy + UNIT_SIZE;
            vertices[count++] = zsz;

            vertices[count++] = zsx;
            vertices[count++] = zsy + UNIT_SIZE;
            vertices[count++] = zsz;

            vertices[count++] = zsx + UNIT_SIZE;
            vertices[count++] = zsy;
            vertices[count++] = zsz;

            vertices[count++] = zsx + UNIT_SIZE;
            vertices[count++] = zsy + UNIT_SIZE;
            vertices[count++] = zsz;

            float[] texcoords=new float[6*2];
            int c=0;
            texcoords[c++] = 0;
            texcoords[c++] = 0;

            texcoords[c++] = 1;
            texcoords[c++] = 0;

            texcoords[c++] = 0;
            texcoords[c++] = 1;

            texcoords[c++] = 0;
            texcoords[c++] = 1;

            texcoords[c++] = 1;
            texcoords[c++] = 0;

            texcoords[c++] = 1;
            texcoords[c++] = 1;

            mVertexBuffer = allocateByteBuffer(vertices.length*4).asFloatBuffer();
            mVertexBuffer.put(vertices);
            mVertexBuffer.position(0);

            mTextureBuffer = allocateByteBuffer(texcoords.length*4).asFloatBuffer();
            mTextureBuffer.put(texcoords);
            mTextureBuffer.position(0);
        }
    }

}
