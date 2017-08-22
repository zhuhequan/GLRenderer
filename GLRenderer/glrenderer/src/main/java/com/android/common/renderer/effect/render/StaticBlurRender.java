/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.FrameBuffer;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.op.DrawTextureOp;
import com.android.common.renderer.effect.texture.Texture;

public class StaticBlurRender extends AbstractBlurRender {
    protected DrawTextureOp mDrawOp = new DrawTextureOp();
    protected DrawInfo mDrawInfo = new DrawInfo();
    private GaussianRender mEffectRender;
    public StaticBlurRender(GLCanvas canvas) {
        super(canvas);
        mEffectRender = GaussianRender.getInstace(canvas);
        mKey = BLUR;
    }

    @Override
    public BlurResult blur2Target(Texture texture, int bitmapWidth, int bitmapHeight) {
        if (skipBlur()) return null;

        int radius = (int)Math.max(mDrawingParameters.getRadius()*mDrawingParameters.getLevel(), 1);
        int passCount = mDrawingParameters.getPassCount();
        float scale = mDrawingParameters.getScale();

        int width = (int)Math.max(bitmapWidth*scale, 1);
        int height = (int)Math.max(bitmapHeight*scale, 1);

        FrameBuffer tmpFrameBuffer = GLRenderer.getFrameBufferPool().get(width, height, true);
        FrameBuffer outFrameBuffer = GLRenderer.getFrameBufferPool().get(width, height, true);
        tmpFrameBuffer.onBind(mGLCanvas);
        outFrameBuffer.onBind(mGLCanvas);
        mDrawInfo.viewportWidth = width;
        mDrawInfo.viewportHeight = height;
        mDrawInfo.drawOp = mDrawOp;
        mGLCanvas.getState().push();
        mGLCanvas.getState().identityModelM();
        mGLCanvas.getState().identityTexM();
        mEffectRender.setRadius(radius);
        for (int i = 0; i < passCount; ++i) {
            mDrawOp.init(i == 0 ? texture : outFrameBuffer.getTexture(), 0, 0, width, height);
            mGLCanvas.getState().setFrameBufferId(tmpFrameBuffer.getId());
            mEffectRender.setDirection(false);
            mEffectRender.draw(mDrawInfo);

            mDrawOp.init(tmpFrameBuffer.getTexture(), 0, 0, width, height);
            mGLCanvas.getState().setFrameBufferId(outFrameBuffer.getId());
            mEffectRender.setDirection(true);
            mEffectRender.draw(mDrawInfo);
        }

        mGLCanvas.getState().pop();
        GLRenderer.getFrameBufferPool().put(tmpFrameBuffer, true);
        mDrawInfo.reset();
        mDrawOp.texture = null;
        return BlurResult.newInstance(outFrameBuffer);
    }
}
