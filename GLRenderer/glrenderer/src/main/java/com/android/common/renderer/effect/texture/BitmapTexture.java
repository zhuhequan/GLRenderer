/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.texture;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.android.common.renderer.RendererUtils;
import com.android.common.renderer.effect.GLCanvas;

public class BitmapTexture extends Texture {
    protected int mGenerationID = -1;
    protected Bitmap mBitmap;
    protected boolean mContentChanged;

    public BitmapTexture(Bitmap bitmap) {
        setBitmap(bitmap);
    }

    public void setBitmap(Bitmap bitmap) {
        RendererUtils.assertTrue(bitmap != null && !bitmap.isRecycled());
        if ((bitmap != mBitmap ||
                bitmap.getGenerationId() != mGenerationID)) {
            mBitmap = bitmap;
            mGenerationID = bitmap.getGenerationId();
            setSize(bitmap.getWidth(), bitmap.getHeight());
            mContentChanged = true;
            mFormat = (bitmap.getConfig() == Bitmap.Config.RGB_565) ?
                      GLES20.GL_RGB565 :
                      GLES20.GL_RGBA;
        }
    }

    public int getGenerationId() {
        return mGenerationID;
    }

    @Override
    protected void upload(GLCanvas canvas) {
        if (mBitmap == null) return;
        if (!isLoaded()) {
            GLES20.glGenTextures(1, sTextureId, 0);
            initTexParameter(getTarget(), sTextureId[0]);
            mId = sTextureId[0];
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            mBitmap = null;
            setLoaded(canvas, true);
            mContentChanged = false;
        } else if (mContentChanged) {
            GLES20.glBindTexture(getTarget(), getId());
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mBitmap);
            mBitmap = null;
            mContentChanged = false;
        }
    }

    @Override
    public int getBytes() {
        return getWidth()*getHeight()*(mFormat == GLES20.GL_RGBA ? 4 : 2);
    }
}
