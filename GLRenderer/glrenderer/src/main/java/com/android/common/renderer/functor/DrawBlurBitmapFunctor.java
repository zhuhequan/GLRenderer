/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.functor;

import android.graphics.Bitmap;
import android.opengl.Matrix;

import com.android.common.renderer.effect.GLCanvasImpl;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.render.AbstractBlurRender;
import com.android.common.renderer.effect.texture.Texture;

public class DrawBlurBitmapFunctor extends AbstractBlurFunctor {
    protected Bitmap mBitmap;

    public void setBitmap(Bitmap bitmap) {
        if (mBitmap != bitmap) {
            mBitmap = bitmap;
            invalidate();
        }
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
    public int getWidth() {
        return mBitmap == null ? 0 : mBitmap.getWidth();
    }

    public int getHeight() {
        return mBitmap == null ? 0 : mBitmap.getHeight();
    }


    public DrawBlurBitmapFunctor(boolean progress) {
        super(progress);
    }

    @Override
    protected void draw(GLCanvasImpl canvas, GLInfo glInfo) {
        if (mBitmap == null || mBitmap.isRecycled()) {
            return;
        }
        Texture texture = GLRenderer.getBitmapTextureCache().get(mBitmap);
        if (!texture.onBind(canvas)) return;


        AbstractBlurRender render = getRender(canvas);
        render.setParameters(mParameters);
        if (mDrawingParameters.copyFrom(mParameters) || mInvalidate || mBlurResult == null) {
            AbstractBlurRender.BlurResult.recycle(mBlurResult, true);
            mBlurResult = render.blur2Target(texture, texture.getWidth(), texture.getHeight());
        }


        mDrawTextureOp.init(AbstractBlurRender.BlurResult.getTexture(mBlurResult, texture),
                            mSourceBounds.left,
                            mSourceBounds.top,
                            mSourceBounds.width(),
                            mSourceBounds.height());
        mDrawInfo.flipProjV = true;
        mDrawInfo.blend = isBlend(texture);
        mDrawInfo.alpha = mAlpha;
        mDrawInfo.viewportWidth = glInfo.viewportWidth;
        mDrawInfo.viewportHeight = glInfo.viewportHeight;
        mDrawInfo.drawOp = mDrawTextureOp;


        if (getOrientation() != 0) {
            Matrix.translateM(canvas.getState().getTexMaxtrix(), 0, 0.5f, 0.5f, 0);
            Matrix.rotateM(canvas.getState().getTexMaxtrix(), 0, getOrientation(), 0, 0, 1);
            Matrix.translateM(canvas.getState().getTexMaxtrix(), 0, -0.5f, -0.5f, 0);
        }

        render.drawResult(mDrawInfo, useOrigTexture() ? texture : null );
        mDrawInfo.reset();
        mInvalidate = false;
    }

    protected int getOrientation() {
        return 0;
    }

    protected boolean useOrigTexture() {
        return false;
    }

}
