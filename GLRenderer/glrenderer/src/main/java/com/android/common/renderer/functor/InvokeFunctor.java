/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.functor;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import android.os.Build;
import android.util.Log;

public class InvokeFunctor extends DrawGLFunctor {
    public void invoke() {
        invokeFunctorInternal(true);
    }

    public void invoke(boolean waitForCompletion) {
        invokeFunctorInternal(waitForCompletion);
    }

    protected boolean invokeFunctorInternal(boolean waitForCompletion) {
        if (mNativeFunctor != 0) {
            if (Build.VERSION.SDK_INT < 21) {
                EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
                if (EGL10.EGL_NO_CONTEXT.equals(context)) {
                    Log.e(TAG, "invokeFunctor fail,sdk version = "+Build.VERSION.SDK_INT);
                    return false;
                }
                onInvoke(kModeProcess);
                return true;
            }
            if (sMethod_invokeFunctor != null) {
                try {
                    sMethod_invokeFunctor.invoke(null, mNativeFunctor, waitForCompletion);
                } catch (Exception e) {
                    Log.e(TAG, "invokeFunctor method doesn't exist"+e.getMessage());
                }
                return true;
            }
        }
        return false;
    }
}
