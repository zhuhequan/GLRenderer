/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
#include <jni.h>
#include "helper.h"

#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved);

#ifdef __cplusplus
}
#endif

JavaVM *gVm = NULL;

extern int register_GLFunctor(JNIEnv* env);
extern int register_EGLBitmap(JNIEnv* env);
extern int register_Utils(JNIEnv* env);

jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;
    gVm = vm;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    if(!env) {
        LOG_E("ERROR:getEnv return null.");
        return -1;
    }

    if (register_GLFunctor(env) < 0) {
        LOG_E("ERROR:register_GLFunctor fail.");
        return -1;
    }

    if (register_Utils(env) < 0) {
        LOG_E("ERROR:register_Utils fail.");
        return -1;
    }

    result = JNI_VERSION_1_4;

    return result;
}
