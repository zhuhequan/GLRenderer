/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
#pragma once
#include <ui/GraphicBuffer.h>
#include <utils/StrongPointer.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include "helper.h"

namespace  android {
class EGLBitmap {
public :
    EGLBitmap(int width, int height, int format, int usage, bool erase, char c);
    EGLBitmap(EGLBitmap *bitmap);
    virtual ~EGLBitmap();
    int bindTexture(int texId);
    void setPixels(void *src, int srcRowBytes);
    void fillPixels(void *dst, int dstRowBytes);
    sp<GraphicBuffer> getBuffer();
private:
    sp<GraphicBuffer> mBuffer;
    EGLImageKHR createEGLImageKHR();
    void copyPixels(char* src, int srcRowBytes, char* dst, int dstRowBytes,int bytesPerPixel);
public:
    int getWidth() {return mBuffer->getWidth();};
    int getHeight() {return mBuffer->getHeight();};
    int getStride() {return mBuffer->getStride();};
    int getFormat() {return mBuffer->getPixelFormat();};
    int getUsage() {return mBuffer->getUsage();};
};
}

