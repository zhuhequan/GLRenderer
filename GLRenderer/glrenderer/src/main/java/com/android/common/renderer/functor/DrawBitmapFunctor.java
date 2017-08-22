/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.functor;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.GLCanvasImpl;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.op.DrawTextureOp;
import com.android.common.renderer.effect.texture.Texture;

public class DrawBitmapFunctor extends DrawGLFunctor {
    protected DrawTextureOp mDrawTextureOp = new DrawTextureOp();
    protected DrawInfo mDrawInfo = new DrawInfo();
    protected Bitmap mBitmap;

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
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

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas, 0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onDraw(GLInfo glInfo) {
        synchronized (this) {
            if (mBitmap == null || mBitmap.isRecycled()) {
                return;
            }
            GLCanvasImpl canvas = GLRenderer.getCanvas();
            canvas.onRenderPreDraw(glInfo);

            Texture texture = GLRenderer.getBitmapTextureCache().get(mBitmap);
            if (!texture.onBind(canvas)) return;

            mDrawInfo.flipProjV = true;
            mDrawInfo.blend = isBlend(texture);
            mDrawInfo.alpha = mAlpha;
            mDrawInfo.viewportWidth = glInfo.viewportWidth;
            mDrawInfo.viewportHeight = glInfo.viewportHeight;
            mDrawInfo.drawOp = mDrawTextureOp;
            mDrawInfo.effectKey = mEffectKey;

            mDrawTextureOp.init(texture,
                    mSourceBounds.left,
                    mSourceBounds.top,
                    mSourceBounds.width(),
                    mSourceBounds.height());

            getRender(canvas).draw(mDrawInfo);
            canvas.onRenderPostDraw();
            mDrawTextureOp.texture = null;
            mDrawInfo.reset();
        }
    }

}

