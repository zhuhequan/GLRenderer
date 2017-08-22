/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GLRecycler {
    private final SynchronizedIntArray mTextureGarbage = new SynchronizedIntArray();
    private final SynchronizedIntArray mBufferGarbage = new SynchronizedIntArray();
    private final SynchronizedIntArray mFrameBufferGarbage = new SynchronizedIntArray();
    private final SynchronizedIntArray mRenderBufferGarbage = new SynchronizedIntArray();
    private final List<Integer> mProgramGarbage = Collections.synchronizedList(new ArrayList<Integer>());
    private static int[] sBuffer = new int[1];


    public void deleteTexture(int id, boolean hasEglContext) {
        if (!hasEglContext) {
            mTextureGarbage.add(id);
        } else {
            sBuffer[0] = id;
            GLES20.glDeleteTextures(1, sBuffer, 0);
        }
    }

    public void deleteBuffer(int id, boolean hasEglContext) {
        if (id == 0) return;
        if (!hasEglContext) {
            mBufferGarbage.add(id);
        } else {
            sBuffer[0] = id;
            GLES20.glDeleteBuffers(1, sBuffer, 0);
        }
    }

    public void deleteRenderBuffer(int id, boolean hasEglContext) {
        if (id == 0) return;
        if (!hasEglContext) {
            mRenderBufferGarbage.add(id);
        } else {
            sBuffer[0] = id;
            GLES20.glDeleteRenderbuffers(1, sBuffer, 0);
        }
    }

    public void deleteFrameBuffer(int id, boolean hasEglContext) {
        if (id == 0) return;
        if (!hasEglContext) {
            mFrameBufferGarbage.add(id);
        } else {
            sBuffer[0] = id;
            GLES20.glDeleteFramebuffers(1, sBuffer, 0);
        }
    }

    public void deleteProgram(int id, boolean hasEglContext) {
        if (id == 0) return;
        if (!hasEglContext) {
            mProgramGarbage.add(id);
        } else {
            GLES20.glDeleteProgram(id);
        }
    }


    public void clearGarbage(boolean hasEglConext) {

        SynchronizedIntArray ids = mTextureGarbage;
        if (ids.size() > 0) {
            if (hasEglConext) GLES20.glDeleteTextures(ids.size(), ids.getInternalArray(), 0);
            ids.clear();
        }

        ids = mBufferGarbage;
        if (ids.size() > 0) {
            if (hasEglConext) GLES20.glDeleteBuffers(ids.size(), ids.getInternalArray(), 0);
            ids.clear();
        }

        ids = mRenderBufferGarbage;
        if (ids.size() > 0) {
            if (hasEglConext) GLES20.glDeleteRenderbuffers(ids.size(), ids.getInternalArray(), 0);
            ids.clear();
        }

        ids = mFrameBufferGarbage;
        if (ids.size() > 0) {
            if (hasEglConext) GLES20.glDeleteFramebuffers(ids.size(), ids.getInternalArray(), 0);
            ids.clear();
        }

        if (hasEglConext) {
            for (int id : mProgramGarbage) {
                GLES20.glDeleteProgram(id);
            }
        }
        mProgramGarbage.clear();
    }

}
