/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.android.common.renderer.effect.texture.RawTexture;
import com.android.common.renderer.effect.texture.Texture;

public class FrameBuffer extends Resource {
    protected int mFrameBufferID[] = new int[1] ;
    protected int mRenderBufferID[] = new int[1] ;
    protected Texture mTexture;
    protected GLCanvas mGLCanvas;
    protected boolean mDepth;
    public FrameBuffer(int width, int height, boolean depth) {
        mTexture = new RawTexture(width, height);
        mDepth = depth;
    }

    public void onBind(GLCanvas canvas) {
        if (mGLCanvas != null && mGLCanvas != canvas) {
            Log.e(GLRenderer.TAG, "FrameBuffer is not release when EGLContext changed.");
            trimResources(GLRenderer.TRIM_MEMORY_COMPLETE, true);
        }
        if (mGLCanvas == null) {
            int width = mTexture.getWidth();
            int height = mTexture.getHeight();
            mTexture.onBind(canvas);
            GLES20.glGenFramebuffers(1, mFrameBufferID, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferID[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D,
                    mTexture.getId(),
                    0);

            mRenderBufferID[0] = 0;
            if (mDepth) {
                GLES20.glGenRenderbuffers(1, mRenderBufferID, 0);
                GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mRenderBufferID[0]);
                GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
                GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                        GLES20.GL_DEPTH_ATTACHMENT,
                        GLES20.GL_RENDERBUFFER,
                        mRenderBufferID[0]);
                GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
            }
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, canvas.getState().getFrameBufferId());
            mGLCanvas = canvas;
        }
    }

    public FrameBuffer(int width, int height) {
        this(width, height, false);
    }

    protected FrameBuffer() {}

    public void clear(GLCanvas canvas, float red, float green, float blue, float alpha) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferID[0]);
        GLES20.glClearColor(red, green, blue, alpha);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, canvas.getState().getFrameBufferId());
    }

    public int getSize() {
        if (mTexture == null) {
            return 0;
        }
        return mTexture.getWidth() * mTexture.getHeight() * (mDepth ? 6 : 4);
    }

    public boolean getDepth() {
        return mDepth;
    }

    public int getWidth() {
        return mTexture.getWidth();
    }

    public int getHeight() {
        return mTexture.getHeight();
    }

    public Texture getTexture() {
        return mTexture;
    }

    public void resetTextureBounds() {
        if (mTexture != null) {
            mTexture.resetBounds();
        }
    }

    public int getId() {
        return mFrameBufferID[0];
    }

    public boolean isEGL() {
        return false;
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        if (mGLCanvas != null) {
            mTexture.trimResources(level, hasEglContext);
            mGLCanvas.deleteFrameBuffer(getId(),hasEglContext);
            if (mDepth && mRenderBufferID[0] != 0) {
                mGLCanvas.deleteRenderBuffer(mRenderBufferID[0],hasEglContext);
                mRenderBufferID[0] = 0;
            }
            mFrameBufferID[0] = 0;
            mGLCanvas = null;
        }
    }

    public void fillBitmap(Bitmap bitmap) {
        if (mGLCanvas != null) {
            EffectUtils.glFillBitmap(bitmap);
        }
    }
}
