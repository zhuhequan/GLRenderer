/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.texture;

import android.opengl.GLES20;

import com.android.common.renderer.effect.GLCanvas;

public class RawTexture extends Texture {
    public RawTexture(int width, int height, int format) {
        setSize(width, height);
        setOpaque(format == GLES20.GL_RGB);
        mFormat = format;
    }

    public RawTexture(int width, int height) {
        this(width, height, GLES20.GL_RGBA);
    }

    @Override
    protected void upload(GLCanvas canvas) {
        if (!isLoaded()) {
            GLES20.glGenTextures(1, sTextureId, 0);
            initTexParameter(getTarget(), sTextureId[0]);

            GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    mFormat,
                    getWidth(),
                    getHeight(),
                    0,
                    mFormat,
                    GLES20.GL_UNSIGNED_BYTE,
                    null);
            mId = sTextureId[0];
            setLoaded(canvas,true);
        }
    }
}
