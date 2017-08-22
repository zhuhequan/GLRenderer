/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import com.android.common.renderer.RendererUtils;
import com.android.common.renderer.SystemProperty;
import com.android.common.renderer.functor.DrawGLFunctor;
import com.android.common.renderer.functor.InvokeFunctor;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

public class GLRenderer {
    public static final String TAG = "glrenderer";

    public static final boolean DEBUG_BLUR = SystemProperty.getBoolean("sys.debug.glrenderer_blur", false);
    public static final boolean DEBUG_FUNCTOR = SystemProperty.getBoolean("sys.debug.glrenderer_functor", false);
    public static final boolean DEBUG_RESOURCE = SystemProperty.getBoolean("sys.debug.glrenderer_resource", false);
    public static final int TRIM_MEMORY_COMPLETE = ComponentCallbacks2.TRIM_MEMORY_COMPLETE;
    public static final int TRIM_MEMORY_MODERATE = ComponentCallbacks2.TRIM_MEMORY_MODERATE;
    public static final int TRIM_MEMORY_UI_HIDDEN = ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN;
    public static final int DENSITY = SystemProperty.getInt("ro.sf.lcd_density",480);

    private static String sLibraryName = "glrenderer";
    private static boolean sInitialized = false;

    private static GLCanvasImpl sGLCanvas;
    private static EGLContext sEGLContext;

    private static Application sAppContext;
    private static TrimMemoryCallback sTrimHideUI;
    private static TrimMemoryCallback sTrimBackground;
    private static TrimMemoryCallback sTrimComplete;

    private static FrameBufferPool sFrameBufferPool ;
    private static TexturePool sTexturePool;
    private static TextureCache sTextureCache;
    private static TrimCallback sTrimCallback;
    private static GLRecycler sGLRecycler = new GLRecycler();

    private final static Object sSync = new Object();

    public static void initialize(Context context) {
        initialize(context, true);
    }

    public static void initialize(Context context, boolean delayed) {
        synchronized (sSync) {
            if (sAppContext != null) return;
            sAppContext = (Application)context.getApplicationContext();
            sTrimCallback = new TrimCallback();
            sAppContext.registerComponentCallbacks(sTrimCallback);
            if (!delayed) {
                loadLibraryIfNeeded();
            }
        }
    }

    public static void loadLibraryIfNeeded() {
        synchronized (sSync) {
            if (sAppContext == null) {
                throw new IllegalStateException("Please call it after initialize. ");
            }
            if (!sInitialized) {
                System.loadLibrary(sLibraryName);
                sInitialized = true;
            }
        }
    }


    //call in render thread
    public static GLCanvasImpl getCanvas() {
        synchronized (sSync) {
            EGLContext context = getCurrentEGLContext();
            if (context != null) {
                if (sGLCanvas == null) {
                    sGLCanvas = new GLCanvasImpl();
                }
                return sGLCanvas;
            }
            return null;
        }
    }

    public static EGLContext getCurrentEGLContext() {
        synchronized (sSync) {
            if (!sInitialized) return null;

            EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
            if (EGL10.EGL_NO_CONTEXT.equals(context) ) {
                Log.e(TAG, "This thread is no EGLContext.");
                return null;
            }
            if (sEGLContext != null) {
                RendererUtils.assertTrue(context.equals(sEGLContext));
            }

            sEGLContext = context;
            return sEGLContext;
        }
    }

    public static Resources getResources() {
        if (sAppContext != null) {
            return sAppContext.getResources();
        }
        return null;
    }


    public static TexturePool getTexturePool() {
        if (sTexturePool == null) {
            synchronized (sSync) {
                sTexturePool = new TexturePool();
                sTexturePool.resize(DENSITY < 640 ? 32 : 60);
            }
        }
        return sTexturePool;
    }

    public static TextureCache getBitmapTextureCache() {
        if (sTextureCache == null) {
            synchronized (sSync) {
                sTextureCache = new TextureCache();
                sTextureCache.resize(DENSITY < 640 ? 42 : 72);
            }
        }
        return sTextureCache;
    }

    public static void trimResources() {
        if (sTrimCallback != null) {
            sTrimCallback.onTrimMemory(TRIM_MEMORY_MODERATE);
        }
    }

    public static FrameBufferPool getFrameBufferPool() {
        if (sFrameBufferPool == null) {
            synchronized (sSync) {
                sFrameBufferPool = new FrameBufferPool();
                sFrameBufferPool.resize(DENSITY < 640 ? 32 : 60);
            }
        }
        return sFrameBufferPool;
    }

    public static GLRecycler getGLRecycler() {
        return sGLRecycler;
    }

    public static Context getAppContext() {
        return sAppContext;
    }

    public static void setLibraryName(String libName) {
        sLibraryName = libName;
    }

    public static void invokeFunctor(InvokeFunctor functor) {
        if (functor != null) {
            functor.invoke();
        }
    }


    private static class TrimMemoryCallback extends InvokeFunctor {
        private int mLevel;

        public TrimMemoryCallback(int level) {
            super();
            mLevel = level;
        }

        @Override
        protected void onInvoke(int what) {
            synchronized (sSync) {
                if (DEBUG_RESOURCE) Log.e(TAG, "trimResources level = "+mLevel);

                boolean hasEglContext = (mLevel < TRIM_MEMORY_COMPLETE);
                final GLCanvasImpl canvas = sGLCanvas;

                if (canvas != null) {
                    canvas.trimResources(mLevel, hasEglContext);
                }

                DrawGLFunctor.freeAllFunctorResouces(mLevel, hasEglContext);
                if (sTexturePool != null) {
                    sTexturePool.trimResources(mLevel, hasEglContext);
                }
                if (sFrameBufferPool != null) {
                    sFrameBufferPool.trimResources(mLevel, hasEglContext);
                }
                if (sTextureCache != null) {
                    sTextureCache.trimResources(mLevel, hasEglContext);
                }

                if (!hasEglContext) {
                    sGLCanvas = null;
                    sEGLContext = null;
                }
                getGLRecycler().clearGarbage(hasEglContext);
            }
        }
    }

    private static class TrimCallback implements ComponentCallbacks2 {
        @Override
        public void onTrimMemory(int level) {
            if (!sInitialized) return;
            if (level >= TRIM_MEMORY_COMPLETE) {
                if (sTrimComplete == null) sTrimComplete = new TrimMemoryCallback(TRIM_MEMORY_COMPLETE);
                sTrimComplete.invoke();
            } else if (level >= TRIM_MEMORY_BACKGROUND) {
                if (sTrimBackground == null) sTrimBackground = new TrimMemoryCallback(TRIM_MEMORY_BACKGROUND);
                sTrimBackground.invoke();
            } else if (level >= TRIM_MEMORY_UI_HIDDEN) {
                if (sTrimHideUI == null) sTrimHideUI = new TrimMemoryCallback(TRIM_MEMORY_UI_HIDDEN);
                sTrimHideUI.invoke();
            }
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {

        }

        @Override
        public void onLowMemory() {

        }

    }
}
