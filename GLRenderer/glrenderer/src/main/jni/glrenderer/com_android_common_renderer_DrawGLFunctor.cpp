/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
#include <jni.h>
#include "DrawGLFunctor.h"
#include "helper.h"
using namespace android;

static jlong GLFunctor_create(JNIEnv *env, jobject thiz, jobject weak_this)
{
    GLFunctor *functor = new GLFunctor(new JNIFunctorContext(env, thiz, weak_this));
    return (jlong) functor;
}

static void GLFunctor_destory(JNIEnv *env, jobject thiz, jlong functorHandle)
{
    if (functorHandle != 0) {
        GLFunctor* functor = reinterpret_cast<GLFunctor*>(functorHandle);
        delete functor;
    }
}

static void GLFunctor_init(JNIEnv *env, jclass clazz){
    JNIFunctorContext::init(env, clazz);
}

static JNINativeMethod gGLFunctorMethods[] = {
    {"native_init", "()V",(void*)GLFunctor_init },
    {"native_destory", "(J)V",(void*)GLFunctor_destory },
    {"native_create", "(Ljava/lang/Object;)J",(void*)GLFunctor_create }
};

int register_GLFunctor(JNIEnv* env)
{
    return registerNativeMethods(env,
            "com/android/common/renderer/functor/DrawGLFunctor",
            gGLFunctorMethods,
            sizeof(gGLFunctorMethods)/sizeof(gGLFunctorMethods[0]));
}
