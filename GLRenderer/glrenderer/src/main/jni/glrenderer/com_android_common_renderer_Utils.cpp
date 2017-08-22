/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
#include <jni.h>
#include <GLES2/gl2.h>
#include <android/bitmap.h>
#include "helper.h"
#include <cmath>

#define FENCE_TIMEOUT 2000000000
#define PI 3.1415926

static void boxBlurH(int* srcPix, int* destPix, int w, int h, int radius)
{

  int index;

  int a = 0, r = 0, g = 0, b = 0;
  int ta, tr, tg, tb;


  int color;
  int preColor;

  int num;
  float iarr;

  for (int i = 0; i < h; ++i)
  {
    r = 0;
    g = 0;
    b = 0;

    index = i * w;
    num = radius;

    for (int j = 0; j < radius; j++)
    {

      color = srcPix[index + j];
      //a += (color & 0xff000000) >> 24;
      r += (color & 0x00ff0000) >> 16;
      g += (color & 0x0000ff00) >> 8;
      b += (color & 0x000000ff);
    }

    for (int j = 0; j <= radius; ++j)
    {
      num++;
      iarr = 1.0 / (1.0 * num);

      color = srcPix[index + j + radius];
      //a += (color & 0xff000000) >> 24;
      r += (color & 0x00ff0000) >> 16;
      g += (color & 0x0000ff00) >> 8;
      b += (color & 0x000000ff);

      //ta = (int)(1.0 * a / num);
      tr = (int)(r * iarr);
      tg = (int)(g * iarr);
      tb = (int)(b * iarr);

      destPix[index + j] = tr << 16 | tg << 8 | tb | 0xff000000;
    }

    iarr = 1.0 / (1.0 * num);
    for (int j = radius + 1; j < w - radius; ++j)
    {
      preColor = srcPix[index + j - 1 - radius];
      color = srcPix[index + j + radius];

      //a += (color & 0xff000000) >> 24 - (preColor & 0xff000000) >> 24;
      r = r + ((color & 0x00ff0000) >> 16) - ((preColor & 0x00ff0000) >> 16);
      g = g + ((color & 0x0000ff00) >> 8)  - ((preColor & 0x0000ff00) >> 8);
      b = b +  (color & 0x000000ff)        -  (preColor & 0x000000ff);

      //ta = (int)(1.0 * a / num);
      tr = (int)(r * iarr);
      tg = (int)(g * iarr);
      tb = (int)(b * iarr);

      destPix[index + j] = tr << 16 | tg << 8 | tb | 0xff000000;
    }

    for (int j = w - radius; j < w; ++j)
    {
      num--;
      iarr = 1.0 / (1.0 * num);

      preColor = srcPix[index + j - 1 - radius];

      //a -= (preColor & 0xff000000) >> 24;
      r -= (preColor & 0x00ff0000) >> 16;
      g -= (preColor & 0x0000ff00) >> 8;
      b -= (preColor & 0x000000ff);

      //ta = (int)(1.0 * a / num);
      tr = (int)(r * iarr);
      tg = (int)(g * iarr);
      tb = (int)(b * iarr);

      //
      //destPix[index + j] = (ta << 24 | tr << 16 | tg << 8 | tb);
      destPix[index + j] = tr << 16 | tg << 8 | tb | 0xff000000;
    }
  }
}

static void boxBlurV(int* srcPix, int* destPix, int w, int h, int radius)
{
  int a = 0, r = 0, g = 0, b = 0;
  int ta, tr, tg, tb;

  int color;
  int preColor;

  int num;
  float iarr;

  for (int i = 0; i < w; ++i)
  {
    r = 0;
    g = 0;
    b = 0;

    num = radius;

    for (int j = 0; j < radius; ++j)
    {
      color = srcPix[j*w + i];
      r += (color & 0x00ff0000) >> 16;
      g += (color & 0x0000ff00) >> 8;
      b += (color & 0x000000ff);
    }

    for (int j = 0; j <= radius; ++j)
    {
      num++;
      iarr = 1.0 / (1.0 * num);

      color = srcPix[(j + radius) * w + i];
      r += (color & 0x00ff0000) >> 16;
      g += (color & 0x0000ff00) >> 8;
      b += (color & 0x000000ff);

      tr = (int)(r * iarr);
      tg = (int)(g * iarr);
      tb = (int)(b * iarr);

      destPix[j*w + i] = tr << 16 | tg << 8 | tb | 0xff000000;
    }

    iarr = 1.0 / (1.0 * num);
    for (int j = radius + 1; j < h - radius; ++j)
    {
      preColor = srcPix[(j - radius - 1) * w + i];
      color = srcPix[(j + radius) * w + i];

      r = r + ((color & 0x00ff0000) >> 16) - ((preColor & 0x00ff0000) >> 16);
      g = g + ((color & 0x0000ff00) >> 8)  - ((preColor & 0x0000ff00) >> 8);
      b = b + (color & 0x000000ff)       - (preColor & 0x000000ff);

      tr = (int)(r * iarr);
      tg = (int)(g * iarr);
      tb = (int)(b * iarr);

      destPix[j*w + i] = tr << 16 | tg << 8 | tb | 0xff000000;
    }

    for (int j = h - radius; j < h; ++j)
    {
      num--;
      iarr = 1.0 / (1.0 * num);
      preColor = srcPix[(j - radius - 1) * w + i];

      r -= (preColor & 0x00ff0000) >> 16;
      g -= (preColor & 0x0000ff00) >> 8;
      b -= (preColor & 0x000000ff);

      tr = (int)(r * iarr);
      tg = (int)(g * iarr);
      tb = (int)(b * iarr);

      destPix[j*w + i] = tr << 16 | tg << 8 | tb | 0xff000000;
    }
  }
}

static void boxBlur(int* srcPix, int* destPix, int w, int h, int r)
{
  boxBlurH(srcPix, destPix, w, h, r);
  boxBlurV(destPix, srcPix, w, h, r);
}

static void boxesForGauss(float sigma, int* size, int n)
{
  float wIdeal = sqrt(12.0 * sigma * sigma / n + 1.0);
  int wl = floor(wIdeal);

  if (0 == wl % 2)
    wl--;

  int wu = wl + 2;

  float mIdeal = (12.0 * sigma * sigma - n * wl * wl - 4 * n * wl - 3 * n) / (-4 * wl - 4);
  int m = round(mIdeal);

  for (int i = 0; i < n; ++i)
    size[i] = (i < m ? wl : wu);
}

static void gaussBlur(int* pix, int w, int h, int r)
{
  float sigma = 1.0 * r / 2.57;

  int boxSize = 3;
  int* boxR = (int*)malloc(sizeof(int) * boxSize);

  boxesForGauss(sigma, boxR, boxSize);

  int* tempPix = (int*)malloc(sizeof(int) * w * h);

  boxBlur(pix, tempPix, w, h, (boxR[0] - 1) / 2);
  boxBlur(pix, tempPix, w, h, (boxR[1] - 1) / 2);
  boxBlur(pix, tempPix, w, h, (boxR[2] - 1) / 2);

  free(boxR);
  free(tempPix);
}


void Utils_glReadPixels(JNIEnv *env, jclass clazz,jobject jbitmap,jint width,jint height) {
    void * pixels = NULL;
    AndroidBitmap_lockPixels(env, jbitmap, &pixels);
    if (pixels) {
        glReadPixels(0, 0, width,height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
    } else {
        LOG_E("Utils_fillPixels bitmap pixels is null");
    }
    AndroidBitmap_unlockPixels(env, jbitmap);
}

void Utils_blurBitmap(JNIEnv* env, jclass clazz, jobject jbitmap, jint r)
{
    void * pixels = NULL;
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env,jbitmap,&info);
    AndroidBitmap_lockPixels(env, jbitmap, &pixels);
    gaussBlur((int*)pixels,info.width,info.height,r);
    AndroidBitmap_unlockPixels(env, jbitmap);
}

static JNINativeMethod gRendererUtilsMethods[] = {
    {"native_blurBitmap", "(Ljava/lang/Object;I)V",(void*)Utils_blurBitmap}
};


static JNINativeMethod gEffectUtilsMethods[] = {
    {"native_glReadPixels", "(Ljava/lang/Object;II)V",(void*)Utils_glReadPixels}
};


int register_Utils(JNIEnv* env)
{
    if (!registerNativeMethods(env,
            "com/android/common/renderer/RendererUtils",
            gRendererUtilsMethods,
            sizeof(gRendererUtilsMethods)/sizeof(gRendererUtilsMethods[0]))) {
        return -1;
    }

    return registerNativeMethods(env,
            "com/android/common/renderer/effect/EffectUtils",
            gEffectUtilsMethods,
            sizeof(gEffectUtilsMethods)/sizeof(gEffectUtilsMethods[0]));

}
