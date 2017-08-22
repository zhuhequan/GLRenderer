package com.android.glede;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.FrameBuffer;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLCanvasImpl;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.op.DrawTextureOp;
import com.android.common.renderer.effect.render.Render;
import com.android.common.renderer.effect.texture.BitmapTexture;
import com.android.common.renderer.functor.DrawGLFunctor;

import java.io.InputStream;

public class DrawGledeFunctor extends DrawGLFunctor {
    private float mAngleX = 0;
    private float mAngleY = 180;
    private float mAngleZ = 0;
    private DrawTextureOp mDrawOp = new DrawTextureOp();
    private DrawInfo mRenderInfo = new DrawInfo();
    private BitmapTexture mTexture;
    private FrameBuffer mFrameBuffer;
    private float mRatio = 0;
    public DrawGledeFunctor() {
        initTexture();
    }

    public void setRatio(float ratio) {
        mRatio = ratio;
    }

    public void setAngleX(float angle) {
        mAngleX = angle;
    }

    public float getAngleX() {
        return mAngleX;
    }

    public void setAngleY(float angle) {
        mAngleY = angle;
    }

    public float getAngleY() {
        return mAngleY;
    }

    public void setAngleZ(float angle) {
        mAngleZ = angle;
    }

    public float getAngleZ() {
        return mAngleZ;
    }

    private void initTexture() {
        if (mTexture == null) {
            InputStream is = GLRenderer.getAppContext().
                    getResources().openRawResource(R.drawable.glede_tex);
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(is);
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                }
                if (bitmap != null){
                    mTexture = new BitmapTexture(bitmap);
                }
            }
        }
    }

    @Override
    public GledeRender getRender(GLCanvas canvas) {
        GledeRender render = (GledeRender)canvas.getRender(GledeRender.KEY_GLEDE);
        if (render == null) {
            render = new GledeRender(canvas);
            canvas.addRender(render);
        }
        return render;
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        super.trimResources(level, hasEglContext);
        if (level > GLRenderer.TRIM_MEMORY_COMPLETE) {
            if (mTexture != null) {
                mTexture.releaseResources(hasEglContext);
                mTexture = null;
            }
            if (mFrameBuffer != null) {
                mFrameBuffer.releaseResources(hasEglContext);
                mFrameBuffer = null;
            }
        }
    }

    @Override
    public void onDraw(GLInfo glInfo) {
        GLCanvasImpl canvas = GLRenderer.getCanvas();
        GledeRender render = getRender(canvas);
        if (render == null) {
            Log.e(TAG, "add GledeRender fail.");
            return;
        }

        canvas.onRenderPreDraw(glInfo);
        /**step 1*/
        int width = mSourceBounds.width();
        int height = mSourceBounds.height();
        if (mFrameBuffer == null ||
                mFrameBuffer.getWidth() != width ||
                mFrameBuffer.getHeight() != height) {
            GLRenderer.getFrameBufferPool().put(mFrameBuffer,true);
            mFrameBuffer = GLRenderer.getFrameBufferPool().get(width, height, true, true);
        }
        mTexture.onBind(canvas);
        mDrawOp.init(mTexture, 0, 0,
                mSourceBounds.width(),
                mSourceBounds.height());
        mRenderInfo.flipProjV = true;
        mRenderInfo.clearFbo = true;
        mRenderInfo.depthTest = true;
        mRenderInfo.cullFace = true;
        mRenderInfo.viewportWidth = width;
        mRenderInfo.viewportHeight = height;
        mRenderInfo.drawOp = mDrawOp;

        canvas.getState().push();
        canvas.getState().identityAllM();
        canvas.getState().setFrameBufferId(mFrameBuffer.getId());
        canvas.getState().translate(0, -5f, -40f);
        if (mAngleX != 0) {
            canvas.getState().rotate(mAngleX, 1, 0, 0);
        }
        if (mAngleY != 0) {
            canvas.getState().rotate(mAngleY, 0, 1, 0);
        }
        if (mAngleZ != 0) {
            canvas.getState().rotate(mAngleZ, 0, 0, 1);
        }
        render.setRatio(mRatio);
        render.draw(mRenderInfo);
        canvas.getState().pop();
        mRenderInfo.reset();

        /**step 2*/
        mDrawOp.init(
                mFrameBuffer.getTexture(),
                mSourceBounds.left,
                mSourceBounds.top,
                mSourceBounds.width(),
                mSourceBounds.height());
        mRenderInfo.flipProjV = true;
        mRenderInfo.viewportWidth = glInfo.viewportWidth;
        mRenderInfo.viewportHeight = glInfo.viewportHeight;
        mRenderInfo.drawOp = mDrawOp;
        mRenderInfo.effectKey = Render.NONE;
        canvas.draw(mRenderInfo);
        mRenderInfo.reset();
        canvas.onRenderPostDraw();
    }
}
