/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.functor;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.util.LongSparseArray;

import com.android.common.renderer.GLRendererNotProguard;
import com.android.common.renderer.RendererUtils;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.Resource;
import com.android.common.renderer.effect.render.Render;
import com.android.common.renderer.effect.texture.Texture;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class DrawGLFunctor extends Resource {
    protected static final String TAG = GLRenderer.TAG;
    // copy from hwui/DrawGlInfo.h
    // Indicates that the functor is called to perform a draw
    public static final int kModeDraw = 0;
    // Indicates the the functor is called only to perform
    // processing and that no draw should be attempted
    public static final int kModeProcess = 1;
    // Same as kModeProcess, however there is no GL context because it was
    // lost or destroyed
    public static final int kModeProcessNoContext = 2;
    // Invoked every time the UI thread pushes over a frame to the render thread
    // *and the owning view has a dirty display list*. This is a signal to sync
    // any data that needs to be shared between the UI thread and the render
    // thread.
    // During this time the UI thread is blocked.
    public static final int kModeSync = 3;
    private static LongSparseArray<WeakReference<DrawGLFunctor>> sDrawGLFunctors =
            new LongSparseArray<WeakReference<DrawGLFunctor>>();
    protected static Method sMethod_callDrawGLFunction;
    protected static Method sMethod_invokeFunctor;

    public static boolean sIsLibInitialized = false;

    protected long mNativeFunctor;

    private Object[] mNativeFunctorObject;
    protected Rect mSourceBounds = new Rect();
    protected String mEffectKey = Render.NONE;
    protected int mAlpha = 0xff;

    public DrawGLFunctor() {
        initLibrary();
        mNativeFunctor = native_create(new WeakReference<DrawGLFunctor>(this));
        RendererUtils.assertTrue(mNativeFunctor != 0);
        mNativeFunctorObject = new Object[1];
        if (Build.VERSION.SDK_INT < 21) {
            mNativeFunctorObject[0] = new Integer((int) mNativeFunctor);
        } else {
            mNativeFunctorObject[0] = new Long(mNativeFunctor);
        }
        synchronized (sDrawGLFunctors) {
            sDrawGLFunctors.put(mNativeFunctor, new WeakReference<DrawGLFunctor>(this));
        }
    }

    public void draw(Canvas canvas) {
        if (canvas.isHardwareAccelerated()) {
            mSourceBounds.set(0, 0, canvas.getWidth(), canvas.getHeight());
            drawFunctorInternal(canvas);
        } else {
            Log.e(TAG, "DrawGLFunctor only can use in hardware accelerated");
        }
    }

    public void draw(Canvas canvas, int left, int top, int right, int bottom) {
        if (canvas.isHardwareAccelerated()) {
            mSourceBounds.set(left, top, right, bottom);
            drawFunctorInternal(canvas);
        } else {
            Log.e(TAG, "DrawGLFunctor only can use in hardware accelerated");
        }
    }
    protected boolean drawFunctorInternal(Canvas canvas) {
        if (mNativeFunctor != 0 && sMethod_callDrawGLFunction != null) {
            try {
                sMethod_callDrawGLFunction.invoke(canvas, mNativeFunctorObject);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "invoke callDrawGLFunction e：" + e.getMessage());
            }
        }
        return false;
    }

    public static void freeAllFunctorResouces(int level, boolean hasEglConext) {
        int size = sDrawGLFunctors.size();
        WeakReference<DrawGLFunctor> refFunctor;
        ArrayList<Long> removeKeys = new ArrayList<Long>();
        for (int i = 0; i < size; ++i) {
            refFunctor = sDrawGLFunctors.get(sDrawGLFunctors.keyAt(i));
            if (refFunctor != null && refFunctor.get() != null) {
                refFunctor.get().trimResources(level, hasEglConext);
            } else {
                removeKeys.add(sDrawGLFunctors.keyAt(i));
            }
        }
        for (long key : removeKeys) {
            sDrawGLFunctors.remove(key);
        }
    }

    public void setEffect(String key) {
        if (key != null) {
            mEffectKey = key;
        }
    }

    public String getEffect() {
        return mEffectKey;
    }

    public Render getRender(GLCanvas canvas) {
        return canvas.getRender(mEffectKey);
    }

    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    public int getAlpha() {
        return mAlpha;
    }

    public boolean isBlend(Texture texture) {
        return (mAlpha != 0xff || (texture != null && !texture.isOpaque()));
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {}
    protected void onInvoke(int what) {}
    public void invalidate() {}

    protected void onDraw(GLInfo glInfo) {
        if (GLRenderer.DEBUG_FUNCTOR) {
            Log.i(GLRenderer.TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Log.i(TAG, String.format("viewport: [%d, %d]",
                    glInfo.viewportWidth, glInfo.viewportHeight));
            Log.i(TAG, String.format("source:[%d, %d, %d, %d]",
                    mSourceBounds.left, mSourceBounds.top, mSourceBounds.right,
                    mSourceBounds.bottom));
            Log.i(TAG, String.format("clip:[%d, %d, %d, %d]", glInfo.clipLeft,
                    glInfo.clipTop, glInfo.clipRight, glInfo.clipBottom));
            Log.i(TAG, "transform: ");

            for (int i = 0; i < 4; i++) {
                Log.i(TAG, String.format("[%.1f, %.1f, %.1f, %.1f]",
                        glInfo.transform[0 + i], glInfo.transform[4 + i],
                        glInfo.transform[8 + i], glInfo.transform[12 + i]));
            }
            Log.i(GLRenderer.TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }
    }

    public static synchronized void initLibrary() {
        if (sIsLibInitialized) return;
        GLRenderer.loadLibraryIfNeeded();
        init();
        sIsLibInitialized = true;
    }

    private static void init() {
        native_init();
        try {
            Class<?> cls = null;
            if (Build.VERSION.SDK_INT < 23) {
                cls = Class.forName("android.view.HardwareCanvas");
                if (Build.VERSION.SDK_INT < 21) {
                    sMethod_callDrawGLFunction = cls.getMethod("callDrawGLFunction", int.class);
                } else if (Build.VERSION.SDK_INT == 21) {
                    sMethod_callDrawGLFunction = cls.getMethod("callDrawGLFunction", long.class);
                } else {
                    sMethod_callDrawGLFunction = cls.getMethod("callDrawGLFunction2", long.class);
                }
            } else {
                cls = Class.forName("android.view.DisplayListCanvas");
                sMethod_callDrawGLFunction = cls.getMethod("callDrawGLFunction2", long.class);
            }

            //Android 5.0之前主线程即为渲染线程
            if (Build.VERSION.SDK_INT >= 21) {
                 cls = Class.forName("android.view.ThreadedRenderer");
                sMethod_invokeFunctor = cls.getDeclaredMethod("invokeFunctor", long.class, boolean.class);
                sMethod_invokeFunctor.setAccessible(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "DrawGLFunctor init fail", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativeFunctor != 0) {
                trimResources(GLRenderer.TRIM_MEMORY_COMPLETE, false);
                native_destory(mNativeFunctor);
                mNativeFunctor = 0;
            }
        } finally {
            super.finalize();
        }
    }

    //call from jni
    @GLRendererNotProguard
    private static void postEventFromNative(WeakReference<DrawGLFunctor> functor,
                                            GLInfo info,
                                            int what) {
        if (functor == null || functor.get() == null)
            return;
        DrawGLFunctor d = functor.get();
        if (info != null) {
            d.onDraw(info);
        } else {
            d.onInvoke(what);
        }
    }

    @GLRendererNotProguard
    private native static void native_init();

    @GLRendererNotProguard
    private native long native_create(Object weak);

    @GLRendererNotProguard
    private native void native_destory(long functor);

    //init from jni
    @GLRendererNotProguard
    public static class GLInfo {
        public int clipLeft;
        public int clipTop;
        public int clipRight;
        public int clipBottom;
        public int viewportWidth;
        public int viewportHeight;
        public float transform[];
        public boolean isLayer;

        public GLInfo() {
            transform = new float[16];
            Matrix.setIdentityM(transform, 0);
        }

        public GLInfo(int width, int height) {
            viewportWidth = width;
            viewportHeight = height;
        }
    }
}
