/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import android.util.Log;

public abstract class Resource {
    public Resource() {
        if (GLRenderer.DEBUG_RESOURCE) {
            Log.e(GLRenderer.TAG,"Create Resource:"+this);
        }
    }

    public abstract void trimResources(int level, boolean hasEglContext);

    public void releaseResources(boolean hasEglContext) {
        trimResources(GLRenderer.TRIM_MEMORY_COMPLETE, hasEglContext);
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
