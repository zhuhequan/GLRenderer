/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.android.common.renderer.effect.texture.BitmapTexture;
import com.android.common.renderer.effect.texture.Texture;

public class TextureCache extends Resource {
    public Texture get(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return null;
        String key = String.valueOf(bitmap.hashCode());
        BitmapTexture texture = mCache.get(key);
        if (texture == null) {
            texture = new BitmapTexture(bitmap);
            mCache.put(key, texture);
        } else if (texture.getGenerationId() != bitmap.getGenerationId()) {
            Log.e(GLRenderer.TAG, "bitmap generation is changed");
            texture.setBitmap(bitmap);
        }
        return texture;
    }

    public void resize(int sizeM) {
        sizeM = Math.max(Math.min(sizeM, 128), 32);
        mCache.resize(sizeM*1024*1024);
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        mCache.trimToSize(0);
    }

    private LruCache<String, BitmapTexture> mCache = new LruCache<String, BitmapTexture>(32*1024*1024) {
        @Override
        protected void entryRemoved(boolean evicted, String key, BitmapTexture oldValue, BitmapTexture newValue) {
            oldValue.releaseResources(false);
        }

        @Override
        protected int sizeOf(String key, BitmapTexture value) {
            return value.getBytes();
        }
    };
}
