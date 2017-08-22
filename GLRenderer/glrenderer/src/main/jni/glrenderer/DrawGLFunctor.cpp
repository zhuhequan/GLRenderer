/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
#include "Utils.h"
#include "DrawGLFunctor.h"
#include "hwui/DrawGlInfo.h"
#include <stdlib.h>
namespace android {


static fields_t gFields;
static jclass gFunctorClass;
static jclass gInfoClass;

static int initFields(JNIEnv *env,jclass clazz) {
    if (!gFields.init) {
        field fields_to_find[] =
        {
            {"com/android/common/renderer/functor/DrawGLFunctor$GLInfo", "clipLeft", "I", &gFields.info_clip_left},
            {"com/android/common/renderer/functor/DrawGLFunctor$GLInfo", "clipTop", "I", &gFields.info_clip_top},
            {"com/android/common/renderer/functor/DrawGLFunctor$GLInfo", "clipRight", "I", &gFields.info_clip_right},
            {"com/android/common/renderer/functor/DrawGLFunctor$GLInfo", "clipBottom", "I", &gFields.info_clip_bottom},
            {"com/android/common/renderer/functor/DrawGLFunctor$GLInfo", "viewportWidth", "I", &gFields.info_viewport_width},
            {"com/android/common/renderer/functor/DrawGLFunctor$GLInfo", "viewportHeight", "I", &gFields.info_viewport_height},
            {"com/android/common/renderer/functor/DrawGLFunctor$GLInfo", "transform", "[F", &gFields.info_transform},
            {"com/android/common/renderer/functor/DrawGLFunctor$GLInfo", "isLayer", "Z", &gFields.info_is_layer}
        };

        if (find_fields(env, fields_to_find, sizeof(fields_to_find)/sizeof(*fields_to_find)) < 0 ) {
            return 0;
        }

        jclass infoClazz = env->FindClass("com/android/common/renderer/functor/DrawGLFunctor$GLInfo");
        if (infoClazz == NULL) {
            LOG_E("Can't find com/android/common/renderer/functor/DrawGLFunctor$GLInfo");
            return 0 ;
        }

        gFields.info_ctor = env->GetMethodID(infoClazz, "<init>", "()V");
        if (gFields.info_ctor == NULL) {
            LOG_E("Can't find com/android/common/renderer/functor/DrawGLFunctor$GLInfo.GLInfo()");
            return 0;
        }

        gFields.post_event= env->GetStaticMethodID(clazz,
            "postEventFromNative" ,
            "(Ljava/lang/ref/WeakReference;Lcom/android/common/renderer/functor/DrawGLFunctor$GLInfo;I)V");

        if (gFields.post_event == NULL) {
            LOG_E("Can't find com/android/common/renderer/functor/DrawGLFunctor.postEventFromNative()");
            return 0;
        }

        gFunctorClass = (jclass)env->NewGlobalRef(clazz);
        gInfoClass = (jclass)env->NewGlobalRef(infoClazz);
        gFields.init = JNI_TRUE;
    }
    return 1;
}

GLFunctor::GLFunctor(JNIFunctorContext* context) {
    mContext = context;
}

GLFunctor:: ~GLFunctor() {
    if (mContext != NULL) {
        delete mContext;
        mContext = NULL;
    }
}

int GLFunctor::operator ()(int what, void* data) {
    //long t = clock();
    switch(what) {
        case DrawGlInfo::kModeDraw:{
            mContext->draw(what, data);
            break;
        }
        case DrawGlInfo::kModeProcess :
        case DrawGlInfo::kModeProcessNoContext:
        case DrawGlInfo::kModeSync:
        {
            mContext->invoke(what);
            break;
        }
    }
    //LOG_W("CallStaticVoidMethod total time = %ld ms", (clock() - t) / 1000);
    return DrawGlInfo::kStatusDone;
}

JNIFunctorContext::JNIFunctorContext(JNIEnv *env, jobject thiz, jobject weak_this) {
    mFunctorObject = env->NewGlobalRef(weak_this);
    mInfoObject = env->NewGlobalRef(env->NewObject(gInfoClass, gFields.info_ctor));
}

JNIFunctorContext::~JNIFunctorContext() {
    JNIEnv *env = getJNIEnv();
    if (mFunctorObject != NULL) {
        env->DeleteGlobalRef(mFunctorObject);
        mFunctorObject= NULL;
    }

    if (mInfoObject != NULL) {
        env->DeleteGlobalRef(mInfoObject);
        mInfoObject= NULL;
    }
}

int JNIFunctorContext::draw(int what, void* data) {
    bool needsDetach = false;
    JNIEnv *env = getJNIEnv();
    if (!env) {
        LOG_E("Has no JNIEnv");
    }

    DrawGlInfo* gl_info = reinterpret_cast<DrawGlInfo*>(data);
    env->SetIntField(mInfoObject, gFields.info_clip_left, gl_info->clipLeft);
    env->SetIntField(mInfoObject, gFields.info_clip_top, gl_info->clipTop);
    env->SetIntField(mInfoObject, gFields.info_clip_right, gl_info->clipRight);
    env->SetIntField(mInfoObject, gFields.info_clip_bottom, gl_info->clipBottom);
    env->SetIntField(mInfoObject, gFields.info_viewport_width, gl_info->width);
    env->SetIntField(mInfoObject, gFields.info_viewport_height, gl_info->height);
    env->SetBooleanField(mInfoObject, gFields.info_is_layer, gl_info->isLayer);

    jfloatArray javaFloatArray = (jfloatArray)env->GetObjectField(mInfoObject, gFields.info_transform);
    jfloat* nativeFloatArray = env->GetFloatArrayElements(javaFloatArray, NULL);

    for (int i = 0; i < 16; ++i) {
      nativeFloatArray[i] = gl_info->transform[i];
    }
    env->ReleaseFloatArrayElements(javaFloatArray, nativeFloatArray, 0);

    env->DeleteLocalRef(javaFloatArray);

    env->CallStaticVoidMethod(gFunctorClass,
                              gFields.post_event,
                              mFunctorObject,
                              mInfoObject,
                              what);

    if (env->ExceptionCheck()) {
        jthrowable t = env->ExceptionOccurred();
        env->ExceptionDescribe();
        env->ExceptionClear();
        LOG_E("FATAL:a exception occurred in your java code when drawing");
        exit(-1);
    }

    return DrawGlInfo::kStatusDone;
}

int JNIFunctorContext::invoke(int what) {
    bool needsDetach = false;
    JNIEnv *env = getJNIEnv();
    if (!env) {
        LOG_E("Has no JNIEnv");
    }

    env->CallStaticVoidMethod(gFunctorClass,
                              gFields.post_event,
                              mFunctorObject,
                              NULL,
                              what);

    if (env->ExceptionCheck()) {
        jthrowable t = env->ExceptionOccurred();
        env->ExceptionDescribe();
        env->ExceptionClear();
        LOG_E("FATAL:a exception occurred in your java code when invoking");
        exit(-1);
    }

    return DrawGlInfo::kStatusDone;
}

void JNIFunctorContext::init(JNIEnv * env, jclass clazz) {
    android::initFields(env, clazz);
}
}
