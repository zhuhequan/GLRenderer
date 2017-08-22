/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
#pragma once
#include <stdlib.h>
#include <jni.h>

#include <android/log.h>

#define LOGTAG "glrenderer"

#define LOG_I(...) ((void)__android_log_print(ANDROID_LOG_INFO, LOGTAG, __VA_ARGS__))
#define LOG_W(...) ((void)__android_log_print(ANDROID_LOG_WARN, LOGTAG, __VA_ARGS__))
#define LOG_E(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOGTAG, __VA_ARGS__))
extern JavaVM *gVm;
static JNIEnv* getJNIEnv() {
    if (!gVm) {
        LOG_E("gVM = 0x%p",gVm);
    }
    JNIEnv* env = NULL;
    if (gVm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOG_E("Failed to obtain JNIEnv");
        return NULL;
    }
    return env;
}

static int registerNativeMethods(JNIEnv* env,
                                 const char* className,
                                 JNINativeMethod* gMethods,
                                 int numMethods)
{
    jclass clazz;
    clazz = env->FindClass(className);

    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}
