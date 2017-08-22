/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.texture;

import android.opengl.GLES20;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLES31Utils;
import com.android.common.renderer.effect.render.ProgressBlurRender;
public class StorageTexture extends Texture {
    public StorageTexture(int width, int height) {
        this(width, height, GLES31Utils.GL_RGBA16F);
    }

    public StorageTexture(int width, int height, int format) {
        mFormat = format;
        setSize(width, height);
    }

    @Override
    protected void upload(GLCanvas canvas) {
        if (!isLoaded()) {
            GLES20.glGenTextures(1, sTextureId, 0);
            initTexParameter(getTarget(), sTextureId[0]);
            ProgressBlurRender.glTexStorage2D(
                    GLES20.GL_TEXTURE_2D,
                    1,
                    mFormat,
                    getWidth(),
                    getHeight());
            mId = sTextureId[0];
            setLoaded(canvas, true);
        }
    }
}

