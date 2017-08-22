/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import android.opengl.GLES20;

import com.android.common.renderer.effect.texture.RawTexture;
import com.android.common.renderer.effect.texture.StorageTexture;
import com.android.common.renderer.effect.texture.Texture;

import java.util.Vector;

//主要用户模糊原始纹理保存
public class TexturePool extends Resource {
    private Vector<Texture> mCache = new Vector<Texture>();
    private int mSize = 0;
    private int mMaxSize;

    public void resize(int sizeM) {
        sizeM = Math.max(Math.min(sizeM, 128), 32);
        mMaxSize = sizeM*1024*1024;
    }

    public  Texture get(int width, int height, int format, boolean hasEglContext) {
        synchronized (this) {
            Texture texture = null;
            int location = -1;
            for (int i=mCache.size()-1; i>=0; --i)  {
                if (mCache.get(i).getWidth() == width &&
                        mCache.get(i).getHeight() == height &&
                        mCache.get(i).getFormat() == format) {
                    location = i;
                    break;
                }
            }

            if (location != -1 ) {
                texture = mCache.get(location);
                removeLocation(location);
            } else if (format == GLES31Utils.GL_RGBA16F ||
                    format == GLES31Utils.GL_RGBA32F) {
                texture = new StorageTexture(width, height, format);
            } else {
                texture = new RawTexture(width, height, format);
            }

            return texture;
        }
    }

    public Texture get(int width, int height, boolean hasEglContext) {
        return get(width, height, GLES20.GL_RGBA, hasEglContext);
    }

    public void put(Texture texture, boolean hasEglContext) {
        synchronized (this) {
            if (texture == null) return;
            texture.resetBounds();
            for (int i=mCache.size()-1; i>=0; --i)  {
                if (mCache.get(i) == texture) {
                    return;
                }
            }
            addNew(texture);
            while (mSize > mMaxSize) {
                removeOldest(hasEglContext);
            }
        }
    }

    private void addNew(Texture texture) {
        mCache.add(texture);
        mSize +=  texture.getBytes();
    }

    private void removeLocation(int location) {
        Texture texture = mCache.remove(location);
        mSize -= texture.getBytes();
    }

    private void removeOldest(boolean hasEglContext) {
        Texture texture = mCache.remove(0);
        mSize -= texture.getBytes();
        texture.releaseResources(hasEglContext);
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        synchronized (this) {
            for (int i=0; i < mCache.size(); ++i)  {
                mCache.get(i).trimResources(level, hasEglContext);
            }
            mCache.clear();
            mSize = 0;
        }
    }
}
