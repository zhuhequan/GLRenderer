/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
#include <jni.h>
#include <GraphicsJNI.h>
#include <android/bitmap.h>
#include "EGLBitmap.h"
#include "helper.h"
using namespace android;
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved);

#ifdef __cplusplus
}
#endif

class BufferWrapper {
public:
    BufferWrapper(const sp<GraphicBuffer>& buffer): buffer(buffer) {}
    sp<GraphicBuffer> buffer;
};

void DeleteBuffer(void* addr, void* context) {
    BufferWrapper* wrapper = (BufferWrapper*) context;
    wrapper->buffer->unlock();
    delete wrapper;
}

static jlong EGLBitmap_alloc(JNIEnv *env, jclass clazz,
    jint width, jint height, jint format, jint usage, jboolean erase, jbyte c)
{
    EGLBitmap *bitmap = new EGLBitmap(width, height, format, usage, erase, c);
    return (jlong) bitmap;
}

static jlong EGLBitmap_dup(JNIEnv *env, jclass clazz, jlong bitmapHandle)
{
    EGLBitmap* bitmap = reinterpret_cast<EGLBitmap *>(bitmapHandle);
    return (jlong)(new EGLBitmap(bitmap));
}

static void EGLBitmap_release(JNIEnv *env, jclass clazz, jlong bitmapHandle)
{
    EGLBitmap* bitmap = reinterpret_cast<EGLBitmap *>(bitmapHandle);
    delete bitmap;
}

static jboolean EGLBitmap_sameAs(JNIEnv *env, jclass clazz, jlong lbitmapHandle, jlong rbitmapHandle)
{
    EGLBitmap* lbitmap = reinterpret_cast<EGLBitmap *>(lbitmapHandle);
    EGLBitmap* rbitmap = reinterpret_cast<EGLBitmap *>(rbitmapHandle);
    return (lbitmap->getBuffer()).get() == (rbitmap->getBuffer()).get();
}

static jboolean EGLBitmap_bindTexture(JNIEnv *env, jclass clazz, jlong bitmapHandle, jint texId)
{
    EGLBitmap *bitmap = reinterpret_cast<EGLBitmap*>(bitmapHandle);
    return (jboolean)bitmap->bindTexture(texId);
}

static void EGLBitmap_setBitmap(JNIEnv *env, jclass clazz,
                               jlong bitmapHandle,
                               jobject jbitmap)
{
    EGLBitmap* bitmap = reinterpret_cast<EGLBitmap*>(bitmapHandle);
    SkBitmap bm;
    GraphicsJNI::getSkBitmap(env, jbitmap, &bm);
    SkAutoLockPixels alp(bm);
    void* addr = bm.getPixels();
    if (addr == NULL) {
        return;
    }
    bitmap->setPixels(addr, bm.rowBytes());
}

static void EGLBitmap_fillBitmap(JNIEnv *env,
                                jclass clazz,
                                jlong bitmapHandle,
                                jobject jbitmap)
{
    EGLBitmap* bitmap = reinterpret_cast<EGLBitmap*>(bitmapHandle);
    SkBitmap bm;
    GraphicsJNI::getSkBitmap(env, jbitmap, &bm);
    SkAutoLockPixels alp(bm);
    void* addr = bm.getPixels();
    if (addr == NULL) {
        return;
    }
    bitmap->fillPixels(addr, bm.rowBytes());
}

static jobject EGLBitmap_getBitmap(JNIEnv *env,
                                   jclass clazz,
                                   jlong bitmapHandle)
{
    EGLBitmap* bitmap = reinterpret_cast<EGLBitmap*>(bitmapHandle);

    sp<GraphicBuffer> buffer = bitmap->getBuffer();
    if (!buffer.get()) {
        return NULL;
    }

    SkColorType colorType;
    SkAlphaType alphaType;
    switch (bitmap->getFormat()) {
        case PIXEL_FORMAT_RGBX_8888: {
            colorType = kRGBA_8888_SkColorType;
            alphaType = kOpaque_SkAlphaType;
            break;
        }
        case PIXEL_FORMAT_RGBA_8888: {
            colorType = kRGBA_8888_SkColorType;
            alphaType = kPremul_SkAlphaType;
            break;
        }
        case PIXEL_FORMAT_RGB_565: {
            colorType = kRGB_565_SkColorType;
            alphaType = kOpaque_SkAlphaType;
            break;
        }
        default: {
            return NULL;
        }
    }

    SkImageInfo imageInfo = SkImageInfo::Make(bitmap->getWidth(),
                                              bitmap->getHeight(), 
                                              colorType, 
                                              alphaType);
                                                   
    void *addr = NULL;
    buffer->lock(GraphicBuffer::USAGE_SW_READ_OFTEN | GraphicBuffer::USAGE_SW_WRITE_OFTEN, &addr);
    if (!addr) {
        LOG_E("EGLBitmap_getBitmap can't lock buffer");
        buffer->unlock();
        return NULL;
    }
    int rowBytes = bitmap->getStride() * android::bytesPerPixel(bitmap->getFormat());
    BufferWrapper *wrapper = new BufferWrapper(buffer);
    Bitmap *jBitmap = new Bitmap(addr, (void*)wrapper, DeleteBuffer, imageInfo, rowBytes, NULL);
    return GraphicsJNI::createBitmap(env, jBitmap, 
            GraphicsJNI::kBitmapCreateFlag_Premultiplied |GraphicsJNI::kBitmapCreateFlag_Mutable,
             NULL);
}

static JNINativeMethod gEGLBitmapMethods[] = {
    {"native_alloc", "(IIII)J", (void*)EGLBitmap_alloc},
    {"native_release", "(J)V", (void*)EGLBitmap_release},
    {"native_dup", "(J)J", (void*)EGLBitmap_dup},
    {"native_sameAs", "(JJ)Z", (void*)EGLBitmap_sameAs},
    {"native_setBitmap", "(JLjava/lang/Object;)V", (void*)EGLBitmap_setBitmap},
    {"native_getBitmap", "(J)Ljava/lang/Object;", (void*)EGLBitmap_getBitmap},
    {"native_fillBitmap", "(JLjava/lang/Object;)V", (void*)EGLBitmap_fillBitmap},
    {"native_bindTexture", "(JI)Z", (void*)EGLBitmap_bindTexture }
};

#define kClassPathName  "com/android/common/renderer/EGLBitmap"

int register_EGLBitmap(JNIEnv* env)
{
    return registerNativeMethods(env, kClassPathName,gEGLBitmapMethods,
        sizeof(gEGLBitmapMethods)/sizeof(gEGLBitmapMethods[0]));
}

jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    if(!env) {
        LOG_E("ERROR:getEnv return null.");
        return -1;
    }
    if (register_EGLBitmap(env) < 0) {
        LOG_E("ERROR:register_EGLBitmap fail.");
        return -1;
    }
    result = JNI_VERSION_1_4;

    return result;
}

