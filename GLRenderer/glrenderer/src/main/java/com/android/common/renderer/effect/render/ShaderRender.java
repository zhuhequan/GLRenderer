/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.texture.Texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

 public abstract class ShaderRender extends Render {
    protected static final float OPAQUE_ALPHA = 0.95f;
    protected int mProgram = 0;
    protected int mUniformMVPMatrixH;
    protected int mUniformSTMatrixH;
    protected int mUniformTextureH;
    protected int mUniformAlphaH;
    protected int mAttributePositionH;
    protected int mAttributeTexCoorH;

    protected abstract void initProgram();
    protected abstract void initShader(DrawInfo drawInfo);

    protected int mCurrentFbo;
    protected boolean mIsCullFace;
    protected boolean mIsDepthTest;
    protected boolean mIsBlend;
    protected boolean mIsScissor;
    public ShaderRender(GLCanvas canvas) {
        super(canvas);
        initProgram();
    }

    public static boolean isOpaque(int color) {
        return (color >>> 24) == 0xff;
    }

    public static void bindTexture(Texture texture, int texure) {
        GLES20.glActiveTexture(texure);
        GLES20.glBindTexture(texture.getTarget(), texture.getId());
    }

    public static void bindTexture(Texture texture) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(texture.getTarget(), texture.getId());
    }

    public static void bindTexture(int textureId, int texure) {
        GLES20.glActiveTexture(texure);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
    }

    public static void bindTexture(int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
    }

    protected void onPreDraw(DrawInfo drawInfo) {
        mGLCanvas.getState().push();
        updateViewport(drawInfo);
        mCurrentFbo = mGLCanvas.getState().getFrameBufferId();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mCurrentFbo);

        mIsBlend = GLES20.glIsEnabled(GLES20.GL_BLEND);
        mIsCullFace = GLES20.glIsEnabled(GLES20.GL_CULL_FACE);
        mIsDepthTest = GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST);
        mIsScissor = GLES20.glIsEnabled(GLES20.GL_SCISSOR_TEST);

        if (drawInfo.blend) {
            GLES20.glEnable(GLES20.GL_BLEND);
        } else {
            GLES20.glDisable(GLES20.GL_BLEND);
        }

        if (drawInfo.cullFace) {
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        } else {
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }

        if (drawInfo.depthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        } else {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }

        if (mCurrentFbo != mGLCanvas.getRootBindingFrameBuffer()) {
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

            if (drawInfo.clearFbo) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            }
        }
    }

    protected void onPostDraw(DrawInfo drawInfo) {
        if (mIsCullFace) {
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        } else {
             GLES20.glDisable(GLES20.GL_CULL_FACE);
        }

        if (mIsDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        } else {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }

        if (mIsScissor) {
            GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        } else {
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        }

        mGLCanvas.getState().pop();
    }

    protected void updateViewport(DrawInfo drawInfo){
        int width = drawInfo.viewportWidth;
        int height = drawInfo.viewportHeight;
        GLES20.glViewport(0, 0, width, height);
        mGLCanvas.getState().identityViewM();
        if (drawInfo.flipProjV) {
            mGLCanvas.getState().orthoM(0, width, height, 0);
        } else {
            mGLCanvas.getState().orthoM(0, width , 0, height);
        }
    }


    @Override
    public boolean draw(DrawInfo drawInfo) {
        return false;
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        if (mProgram != 0 && mGLCanvas != null) {
            mGLCanvas.deleteProgram(mProgram, hasEglContext);
            mProgram = 0;
            mKey = null;
            mGLCanvas = null;
        }
        super.trimResources(level, hasEglContext);
    }


    protected String getVertexShader() {
        return VERTEX;
    }

    protected String getFragmentShader() {
        return FRAG;
    }

    public static ByteBuffer allocateByteBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    private static final String VERTEX =
            "uniform mat4 uMVPMatrix; \n"+
            "uniform mat4 uSTMatrix;\n"+
            "attribute vec3 aPosition;\n"+
            "attribute vec2 aTexCoord;\n"+
            "varying vec2 vTexCoord;\n"+
            "void main() {\n"+
            "    gl_Position = uMVPMatrix * vec4(aPosition,1);\n"+
            "    vTexCoord = (uSTMatrix * vec4(aTexCoord,0,1)).st;\n"+
            "}";

    private static final String FRAG=
            "precision mediump float;\n"+
            "uniform sampler2D sTexture;\n"+
            "uniform float uAlpha;\n"+
            "varying vec2 vTexCoord;\n"+

            "void main() {\n"+
            "    gl_FragColor = texture2D(sTexture, vTexCoord);\n" +
            "    gl_FragColor.a *= uAlpha; \n"+
            "}\n";

}

