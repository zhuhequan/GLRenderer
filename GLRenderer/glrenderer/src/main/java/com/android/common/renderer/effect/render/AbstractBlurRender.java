/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.util.Log;

import com.android.common.renderer.RendererUtils;
import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.FrameBuffer;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.effect.ObjectCache;
import com.android.common.renderer.effect.op.DrawTextureOp;
import com.android.common.renderer.effect.texture.Texture;

abstract public class AbstractBlurRender extends Render {
    protected final Parameters mDrawingParameters;
    public AbstractBlurRender(GLCanvas canvas) {
        super(canvas);
        mDrawingParameters = new Parameters();
    }

    abstract public BlurResult blur2Target(Texture texture, int bitmapWidth, int bitmapHeight);

    public BlurResult blur2Target(DrawTextureOp element) {
        return blur2Target(element.texture, element.texture.getWidth(), element.texture.getHeight());
    }

    public void drawResult(DrawInfo drawInfo, Texture origTexture) {
        BlurFilterRender render = BlurFilterRender.getInstace(mGLCanvas);
        render.setOrigTexture(origTexture);
        render.setFilterColor(mDrawingParameters.getFilterColor());
        render.setIntensity(mDrawingParameters.getIntensity());
        render.setOrigTexture(null);
        render.draw(drawInfo);
    }

    public void drawResult(DrawInfo drawInfo) {
        drawResult(drawInfo, null);
    }


    @Override
    public boolean draw(DrawInfo drawInfo) {
        DrawTextureOp element = (DrawTextureOp)drawInfo.drawOp;
        BlurResult output = blur2Target(element);
        if (output != null) {
            element.texture = output.getTexture();
        }
        drawResult(drawInfo);
        output.recycle(true);
        return true;
    }

    public boolean setParameters(Parameters p) {
        return mDrawingParameters.copyFrom(p);
    }

    public Parameters getParameters() {
        return mDrawingParameters;
    }

    protected boolean skipBlur() {
        return mDrawingParameters.getLevel() < 0.01f;
    }

    public static class BlurResult {
        private static final ObjectCache<BlurResult> CACHED =
                new ObjectCache<BlurResult>(BlurResult.class,12);
        private boolean useTexture;
        private boolean recycled;
        private FrameBuffer framebuffer;
        private Texture texture;

        private boolean isRecycled() {
            return recycled;
        }

        private void recycle(boolean hasEglContext) {
            if (isRecycled()) {
                return;
            }
            if (useTexture) {
                GLRenderer.getTexturePool().put(texture, hasEglContext);
                texture = null;
            } else {
                GLRenderer.getFrameBufferPool().put(framebuffer, hasEglContext);
                framebuffer = null;
            }
            recycled = true;
            CACHED.put(this);
        }

        private Texture getTexture() {
            return useTexture ? texture : framebuffer.getTexture();
        }


        public static BlurResult newInstance(Object o) {
            BlurResult cached = CACHED.pop();
            cached.recycled = false;
            if (o instanceof FrameBuffer) {
                cached.framebuffer = (FrameBuffer) o;
                cached.useTexture = false;
            } else {
                cached.texture = (Texture) o;
                cached.useTexture = true;
            }
            return cached;
        }


        public static Texture getTexture(BlurResult o, Texture defaultText) {
            if (o == null) return defaultText;
            return o.getTexture();
        }

        public static void recycle(BlurResult o, boolean hasEglContext) {
            if (o != null) {
                o.recycle(hasEglContext);
            }
        }
    }

    public static class Parameters {
        private float mLevel = 1f;
        private int mRadius = 4;
        private float mScale = 0.06f;
        private int mPassCount = 2;
        private int mFilterColor = 0;
        private float mIntensity = 1f;

        private boolean mInvalidate = true;
        public Parameters() {
        }

        public void setLevel(float level) {
            if (Math.abs(level - mLevel) > 0.005f) {
                mLevel = level;
                mInvalidate = true;
            }
        }

        public float getLevel() {
            return mLevel;
        }

        public void setRadius(int radius) {
            if (mRadius != radius) {
                mRadius = radius;
                mInvalidate = true;
            }
        }

        public int getRadius() {
            return mRadius;
        }


        public void setScale(float scale) {
            scale = RendererUtils.clip(scale, 0.001f, 1.01f);
            if (Math.abs(mScale - scale) > 0.001f) {
                mScale = scale;
                mInvalidate = true;
            }
        }

        public float getScale() {
            return mScale;
        }


        public void setPassCount(int count) {
            if (mPassCount != count) {
                mPassCount = count;
                mInvalidate = true;
            }
        }

        public int getPassCount() {
            return mPassCount;
        }

        public void setFilterColor(int color) {
            if (mFilterColor != color) {
                mFilterColor = color;
                mInvalidate = true;
            }
        }

        public int getFilterColor() {
            return mFilterColor;
        }

        public void setIntensity(float intensity) {
            mIntensity = intensity;
        }

        public float getIntensity() {
            return mIntensity;
        }

        public boolean copyFrom(Parameters p) {
            mInvalidate = false;
            setLevel(p.getLevel());
            setRadius(p.getRadius());
            setScale(p.getScale());
            setPassCount(p.getPassCount());
            setIntensity(p.getIntensity());
            setFilterColor(p.getFilterColor());
            return mInvalidate;
        }

        public void dump() {
            Log.i(GLRenderer.TAG, "level = " + mLevel);
            Log.i(GLRenderer.TAG, "radius = " + mRadius);
            Log.i(GLRenderer.TAG, "scale = " + mScale);
            Log.i(GLRenderer.TAG, "passCount = " + mPassCount);
            Log.i(GLRenderer.TAG, "intensity = " + mIntensity);
        }
    }
}
