/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */

#include <EGL/egl.h>
#include <EGL/eglext.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "EGLBitmap.h"

namespace android {

EGLBitmap::EGLBitmap(int width, int height, int format, int usage, bool erase, char c) {
    mBuffer = new GraphicBuffer(width, height, format, usage);
    if (mBuffer.get()) {
        status_t err = mBuffer->initCheck();
        if (err != NO_ERROR) {
            LOG_E("New EGLBitmap fail %s", strerror(-err));
            mBuffer = NULL;
            return;
        }
        if (erase) {
            void* vaddr = NULL;
            mBuffer->lock(GraphicBuffer::USAGE_SW_WRITE_OFTEN, &vaddr);
            if (!vaddr) {
                mBuffer->unlock();
                return ;
            }
            memset(vaddr, c, getHeight()*getStride()*android::bytesPerPixel(getFormat()));
            mBuffer->unlock();
        }
    }
}

EGLBitmap::EGLBitmap(EGLBitmap *bitmap) {
    mBuffer = bitmap->mBuffer;
}

EGLBitmap::~EGLBitmap() {; 
    mBuffer = NULL;
}

void EGLBitmap::setPixels(void * src, int srcRowBytes) {
    if (!mBuffer.get()) {
        return;
    }
    void* dst = NULL;
    mBuffer->lock(GraphicBuffer::USAGE_SW_WRITE_OFTEN, &dst);
    if (!dst) {
        LOG_E("setPixels can't lock buffer");
        mBuffer->unlock();
        return;
    }
    int bytesPerPixel = android::bytesPerPixel(getFormat());
    copyPixels((char*)src, srcRowBytes, (char*)dst, getStride()*bytesPerPixel, bytesPerPixel);
    mBuffer->unlock();
}

void EGLBitmap::fillPixels(void* dst, int dstRowbytes) {
    if (!mBuffer.get()) {
        LOG_E("getPixels mBuffer is NULL");
        return;
    }

    void* src = NULL;
    mBuffer->lock(GraphicBuffer::USAGE_SW_READ_OFTEN, &src);
    if (!src) {
        LOG_E("getPixels can't lock buffer");
        mBuffer->unlock();
        return;
    }
    int bytesPerPixel = android::bytesPerPixel(getFormat());
    copyPixels((char*)src, getStride()*bytesPerPixel, (char*)dst, dstRowbytes, bytesPerPixel);
    mBuffer->unlock();
}

sp<GraphicBuffer> EGLBitmap::getBuffer() {
    return mBuffer;
}

void EGLBitmap::copyPixels(char* src, int srcRowBytes, char* dst, int dstRowBytes, int bytesPerPixel) {
    int width = getWidth();
    int height = getHeight();
    if (srcRowBytes == dstRowBytes) {
        memcpy(dst, src, srcRowBytes*height);
    } else {
        for (int i = 0; i < height; i++) {
            memcpy(dst, src, width * bytesPerPixel);
            dst += dstRowBytes;
            src += srcRowBytes;
        }
    }
}

int EGLBitmap::bindTexture(int texId) {
    EGLImageKHR imageKHR = createEGLImageKHR();
    if (imageKHR == EGL_NO_IMAGE_KHR) {
        return 0;
    }
    glBindTexture(GL_TEXTURE_2D, texId);
    glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, (GLeglImageOES)imageKHR);
    eglDestroyImageKHR(eglGetCurrentDisplay(), imageKHR);
    return 1;
}

EGLImageKHR EGLBitmap::createEGLImageKHR() {
    if (!mBuffer.get()) {
        return EGL_NO_IMAGE_KHR;
    }

    EGLint attrs[] = {
        EGL_IMAGE_PRESERVED_KHR, EGL_TRUE,
        EGL_NONE,
    };
    android_native_buffer_t* anb = mBuffer->getNativeBuffer();
    if (anb == NULL){
        LOG_E("getNativeBuffer failed");
        return EGL_NO_IMAGE_KHR;
    }
    EGLImageKHR imageKHR = eglCreateImageKHR(eglGetCurrentDisplay(),
                                             EGL_NO_CONTEXT,
                                             EGL_NATIVE_BUFFER_ANDROID,
                                             (EGLClientBuffer)anb,
                                             attrs);
    if (imageKHR == EGL_NO_IMAGE_KHR) {
        LOG_E("eglCreateImageKHR fail %#x",eglGetError());
        return EGL_NO_IMAGE_KHR;
    }
    return imageKHR;
}
}

