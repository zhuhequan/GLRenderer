/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.functor;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.util.Log;

import com.android.common.renderer.effect.EffectUtils;
import com.android.common.renderer.effect.GLCanvasImpl;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.render.AbstractBlurRender;
import com.android.common.renderer.effect.texture.Texture;

public class DrawBlurFunctor extends AbstractBlurFunctor {
    private Texture mBgTexture;
    private Rect mSourceClipBounds = new Rect();

    private boolean mIsStatic = false;
    private boolean mIgnoreLayer = false;
    private boolean mRegionUpdate = false;

    public DrawBlurFunctor(boolean progress) {
        super(progress);
    }

    @Override
    protected void draw(GLCanvasImpl canvas, GLInfo glInfo) {
        if (!caculaTargetBounds(glInfo)) {
            return;
        }

        AbstractBlurRender render = getRender(canvas);
        render.setParameters(mParameters);
        if (copyTexture(canvas, glInfo) ||
                mDrawingParameters.copyFrom(mParameters) ||
                mBlurResult == null) {
            if (GLRenderer.DEBUG_BLUR) Log.i(TAG,"do blur..");
            AbstractBlurRender.BlurResult.recycle(mBlurResult, true);
            mBlurResult = render.blur2Target(mBgTexture, mBgTexture.getWidth(), mBgTexture.getHeight());
        }

        mDrawTextureOp.init(
                AbstractBlurRender.BlurResult.getTexture(mBlurResult, mBgTexture),
                mSourceBounds.left,
                mSourceBounds.top,
                mSourceBounds.width(),
                mSourceBounds.height());

        mDrawInfo.flipTextureV = true;
        mDrawInfo.flipProjV = true;
        mDrawInfo.blend = isBlend(mBgTexture);
        mDrawInfo.alpha = mAlpha;
        mDrawInfo.viewportWidth = glInfo.viewportWidth;
        mDrawInfo.viewportHeight = glInfo.viewportHeight;
        mDrawInfo.drawOp = mDrawTextureOp;
        mDrawInfo.effectKey = mEffectKey;
        render.drawResult(mDrawInfo);


        if (!mRegionUpdate && !isStatic()) {
            GLRenderer.getTexturePool().put(mBgTexture, true);
            mBgTexture = null;
        }

        mDrawInfo.reset();
        mInvalidate = false;
    }

    /**
     * 复制纹理，即当前view底下的纹理图片，如果不是静态的每次绘制都会获取一次
     * @param canvas
     * @param glInfo
     * @return
     */
    private boolean copyTexture(GLCanvasImpl canvas, GLInfo glInfo) {
        if (isStatic() && !mInvalidate && mBgTexture != null) {
            return false;
        }

        int width = mSourceBounds.width();
        int height = mSourceBounds.height();
        if (mBgTexture == null ||
                mBgTexture.getWidth() != width ||
                mBgTexture.getHeight() != height) {
            GLRenderer.getTexturePool().put(mBgTexture, true);
            mBgTexture = GLRenderer.getTexturePool().get(width, height, GLES20.GL_RGB, true);
        }

        if (GLRenderer.DEBUG_BLUR && glInfo.isLayer) {
            Log.i(TAG, "has Layer");
        }

        //如果设置了View的alpha或layerType当前fbo将不会为0
        if (glInfo.isLayer && mIgnoreLayer) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }

        mBgTexture.bindTexture(canvas);
        GLES20.glCopyTexSubImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                Math.abs(mSourceClipBounds.left - mSourceBounds.left),
                Math.abs(mSourceClipBounds.bottom - mSourceBounds.bottom),
                mTargetBounds.left,
                glInfo.viewportHeight - mTargetBounds.bottom,
                mTargetBounds.width(),
                mTargetBounds.height());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, canvas.getRootBindingFrameBuffer());
        return true;
    }

    public void setStatic(boolean isStatic) {
        mIsStatic = isStatic;
    }


    private boolean caculaTargetBounds(GLInfo glInfo) {
        //此处不考虑缩放和旋转，如果有View有缩放和旋转复制的区域可能不准确
        EffectUtils.window2View(glInfo.transform,
                glInfo.clipLeft, glInfo.clipTop, glInfo.clipRight, glInfo.clipBottom,
                mSourceClipBounds);
        //如果不相交说明这个funtor不需要绘制
        if (mSourceClipBounds.intersect(mSourceBounds)) {
            EffectUtils.view2Window(glInfo.transform, mSourceClipBounds, mTargetBounds);
            return true;
        } else {
            Log.e(TAG, "Clip region is not intersected with source region.");
            return false;
        }
    }

    public void setIgnoreLayer(boolean ignore) {
        mIgnoreLayer = ignore;
    }

    public void setRegionUpdate(boolean regionUpdate) {
        mRegionUpdate = regionUpdate;
    }

    public boolean isStatic() {
        return mIsStatic;
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        super.trimResources(level, false);
        synchronized (this) {
            if (level >= GLRenderer.TRIM_MEMORY_UI_HIDDEN) {
                if (mBgTexture != null) {
                    GLRenderer.getTexturePool().put(mBgTexture, hasEglContext);
                    mBgTexture = null;
                }
            }
            mInvalidate = true;
        }
    }
}
