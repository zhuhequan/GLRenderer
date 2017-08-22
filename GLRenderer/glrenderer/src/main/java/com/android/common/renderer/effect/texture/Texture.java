/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.texture;

import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.Resource;

public abstract class Texture extends Resource {
    protected static final String TAG = GLRenderer.TAG;
    private static final int MAX_TEXTURE_SIZE = 3072;

    protected static int[] sTextureId = new int[1];

    protected int mWidth = -1;
    protected int mHeight = -1;

    protected RectF mBounds = new RectF(0, 0, 1, 1);
    protected boolean mBoundsChanged ;
    protected boolean mOpaque ;
    protected GLCanvas mGLCanvas;
    protected int mId;
    protected boolean mLoaded;
    protected int mFormat;


    public void initTexParameter(int target, int textureId) {
        GLES20.glBindTexture(target, textureId);
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
    }

    public void updateTransformMatrix(GLCanvas canvas, boolean flipX, boolean flipY) {
        boolean changed = mBoundsChanged || flipX || flipY;
        if (changed) {
            float[] matrix = canvas.getState().getTexMaxtrix();
            float left = flipX ? mBounds.right : mBounds.left;
            float top = flipY ? mBounds.bottom : mBounds.top;
            float width = flipX ? -mBounds.width() : mBounds.width();
            float height = flipY ? -mBounds.height() : mBounds.height();
            Matrix.translateM(matrix, 0, left, top, 0);
            Matrix.scaleM(matrix, 0, width, height, 1.0f);
        }
    }

    public void setSize(int width, int height) {
        if (isLoaded() && (mWidth != width || mHeight != height)) {
             trimResources(GLRenderer.TRIM_MEMORY_COMPLETE, false);
        }
        mWidth = width;
        mHeight = height;
        checkSize();
    }

    public int getBytes() {
        return getWidth()*getHeight()*(mFormat == GLES20.GL_RGB ? 3 : 4);
    }

    public boolean onBind(GLCanvas canvas) {
        upload(canvas);
        return isLoaded();
    }

    protected void upload(GLCanvas canvas) {}

    public void bindTexture(GLCanvas canvas) {
        upload(canvas);
        GLES20.glBindTexture(getTarget(), getId());
    }

    public int getId() {
        return mId;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void resetBounds() {
        mBounds.set(0, 0, 1, 1);
        mBoundsChanged = false;
    }

    public RectF getBounds() {
        return mBounds;
    }

    public void setBounds(float x, float y, float width, float height) {
        mBounds.set(x, y, x+width, y+height);
        mBoundsChanged = true;
    }

    public int getFormat() {
        return mFormat;
    }

    public int getTarget() {
        return GLES20.GL_TEXTURE_2D;
    }

    protected void setLoaded(GLCanvas canvas, boolean loaded) {
        mGLCanvas = canvas;
        mLoaded = loaded;
    }

    public boolean isLoaded() {
        return mGLCanvas != null && mLoaded;
    }

    public void setOpaque(boolean isOpaque) {
        mOpaque = isOpaque;
    }

    public boolean isOpaque() {
        return mOpaque;
    }

    private void checkSize() {
        if (mWidth > MAX_TEXTURE_SIZE || mHeight > MAX_TEXTURE_SIZE) {
            Log.w(TAG, String.format("Texture is %d x %d",mWidth, mHeight),new Exception());
        }
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        if (isLoaded()) {
            mGLCanvas.deleteTexture(getId(), hasEglContext);
        }
        setLoaded(null, false);
    }

}
