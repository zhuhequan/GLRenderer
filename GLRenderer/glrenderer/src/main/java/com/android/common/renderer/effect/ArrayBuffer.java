/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;


import java.nio.FloatBuffer;

import android.opengl.GLES20;

public class ArrayBuffer extends Resource{
    protected int mBufferID[] = new int[1] ;
    protected GLCanvas mGLCanvas;

    public ArrayBuffer(GLCanvas canvas, FloatBuffer buffer) {
        GLES20.glGenBuffers(1, mBufferID, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBufferID[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                            buffer.capacity() * (Float.SIZE / Byte.SIZE),
                            buffer,
                            GLES20.GL_STATIC_DRAW);
        mGLCanvas = canvas;
    }

    public int getId() {
        return mBufferID[0];
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        if (mGLCanvas != null) {
            mGLCanvas.deleteBuffer(getId(), hasEglContext);
            mBufferID[0] = 0;
            mGLCanvas = null;
        }
    }

    public void bindSelf() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBufferID[0]);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            trimResources(GLRenderer.TRIM_MEMORY_COMPLETE, false);
        } finally {
            super.finalize();
        }
    }
}
