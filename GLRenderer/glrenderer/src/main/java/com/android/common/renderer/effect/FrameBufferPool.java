/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import java.util.Vector;


public class FrameBufferPool extends Resource {
    private Vector<FrameBuffer> mCache = new Vector<FrameBuffer>();
    private int mMaxSize = 0;
    private int mSize = 0;

    public void resize(int sizeM) {
        sizeM = Math.max(Math.min(sizeM, 128), 32);
        mMaxSize = sizeM*1024*1024;
    }

    public FrameBuffer get(int width, int height, boolean hasEglContext) {
        return get(width, height, false, hasEglContext);
    }

    public FrameBuffer get(int width, int height, boolean depth, boolean hasEglContext) {
        synchronized (this) {
            FrameBuffer framebuffer = null;
            int location = -1;
            for (int i=mCache.size()-1; i>=0; --i)  {
                if (mCache.get(i).getWidth() == width &&
                        mCache.get(i).getHeight() == height &&
                        mCache.get(i).getDepth() == depth) {
                    location = i;
                    break;
                }
            }
            if (location != -1 ) {
                framebuffer = mCache.get(location);
                removeLocation(location);
            } else {
                framebuffer = new FrameBuffer(width, height, depth);
            }
            if (hasEglContext) {
                framebuffer.onBind(GLRenderer.getCanvas());
            }
            return framebuffer;
        }
    }

    public void put(FrameBuffer frameBuffer, boolean hasEglContext) {
        if (frameBuffer == null) return;
        synchronized (this) {
            frameBuffer.resetTextureBounds();
            for (int i=mCache.size()-1; i>=0; --i)  {
                if (mCache.get(i) == frameBuffer) {
                    return;
                }
            }
            addNew(frameBuffer);
            while (mSize > mMaxSize) {
                removeOldest(hasEglContext);
            }
        }
    }

    private void addNew(FrameBuffer frameBuffer) {
        mCache.add(frameBuffer);
        mSize +=  frameBuffer.getSize();
    }

    private void removeLocation(int location) {
        FrameBuffer framebuffer = mCache.remove(location);
        mSize -= framebuffer.getSize();
    }

    private void removeOldest(boolean hasEglContext) {
        FrameBuffer framebuffer = mCache.remove(0);
        mSize -= framebuffer.getSize();
        framebuffer.releaseResources(hasEglContext);
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        synchronized (this) {
            for (int i=0; i<mCache.size(); ++i)  {
                mCache.get(i).trimResources(level, hasEglContext);
            }
            mCache.clear();
            mSize = 0;
        }
    }
}
