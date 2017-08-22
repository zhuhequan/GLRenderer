/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
#pragma once
#include <jni.h>
#include <pthread.h>
#include "helper.h"

namespace  android {
class JNIFunctorContext {
public:
     JNIFunctorContext(JNIEnv *env, jobject thiz, jobject weak_this);
     ~JNIFunctorContext();
     int draw(int what, void* data);
     int invoke(int what);
     static void init(JNIEnv* env,jclass clazz);
private:
    jobject mFunctorObject;
    jobject mInfoObject;
};

class Functor {
public:
    Functor() {}
    virtual ~Functor() {}
    virtual int operator ()(int /*what*/, void* /*data*/) { return 0; }
};

class GLFunctor : public Functor {
public :
    GLFunctor(JNIFunctorContext* context);
    ~GLFunctor();
     int operator ()(int what, void* data);
private:;
    JNIFunctorContext* mContext;
};

}
