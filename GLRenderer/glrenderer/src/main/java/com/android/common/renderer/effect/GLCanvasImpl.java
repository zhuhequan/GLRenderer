/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import android.opengl.GLES20;
import android.util.Log;

import com.android.common.renderer.effect.render.FishEyeRender;
import com.android.common.renderer.effect.render.FogRender;
import com.android.common.renderer.effect.render.GrayRender;
import com.android.common.renderer.effect.render.MosaicRender;
import com.android.common.renderer.effect.render.NoneRender;
import com.android.common.renderer.effect.render.StaticBlurRender;
import com.android.common.renderer.effect.render.Render;
import com.android.common.renderer.effect.render.RenderGroup;
import com.android.common.renderer.effect.render.SeventyRender;
import com.android.common.renderer.effect.render.SketchEffectRender;
import com.android.common.renderer.effect.render.VividRender;
import com.android.common.renderer.effect.render.WaterRender;
import com.android.common.renderer.effect.render.YesteryearRender;
import com.android.common.renderer.functor.DrawGLFunctor.GLInfo;

public class GLCanvasImpl extends Resource implements GLCanvas {
    private StateMachine mSnapshot;
    private int mBindingFrameBuffer = 0;
    private RenderGroup mRenderGroup;
//    private boolean mIsScissor;
    public GLCanvasImpl() {
        mSnapshot = new StateMachine();
        mRenderGroup = new RenderGroup(this);
    }

    @Override
    public Render getRender(String key) {
        Render render = mRenderGroup.getRender(key);
        if (render != null) return render;
        render = createRender(key);
        if (render != null) {
            mRenderGroup.addRender(render);
        }
        return render;
    }

    @Override
    public void addRender(Render render) {
        if (render != null) {
            if (render.getKey().equals(Render.NONE) ) {
                Log.e(GLRenderer.TAG,"Add render fail ,key = "+render.getKey());
                return;
            }
            mRenderGroup.addRender(render);
        }
    }


    @Override
    public StateMachine getState() {
        return mSnapshot;
    }

    @Override
    public int getRootBindingFrameBuffer() {
        return mBindingFrameBuffer;
    }

    @Override
    public void draw(DrawInfo drawInfo) {
        getRender(drawInfo.effectKey).draw(drawInfo);
    }


    public void onRenderPreDraw(GLInfo glInfo) {
        mSnapshot.reset();
        mSnapshot.setMatrix(glInfo.transform, 0);

        int fbo[] = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, fbo,0);
        mSnapshot.setFrameBufferId(fbo[0]);
        mBindingFrameBuffer = fbo[0];

//        mIsScissor = GLES20.glIsEnabled(GLES20.GL_SCISSOR_TEST);
//        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

        GLES20.glClearColor(1f, 1f, 1f, 1f);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLRenderer.getGLRecycler().clearGarbage(true);
    }

    public void onRenderPostDraw() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mBindingFrameBuffer);
//        if (mIsScissor) {
//            GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
//        } else {
//            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
//        }
        GLRenderer.getGLRecycler().clearGarbage(true);
    }


    @Override
    public void trimResources(int level, boolean hasEglContext) {
        if (level >= GLRenderer.TRIM_MEMORY_UI_HIDDEN) {
            mSnapshot.recycle();
        }

        if (level > GLRenderer.TRIM_MEMORY_MODERATE) {
            mRenderGroup.trimResources(level, hasEglContext);
        }
    }


    @Override
    public void deleteTexture(int texture, boolean hasEglContext) {
        GLRenderer.getGLRecycler().deleteTexture(texture, hasEglContext);
    }

    @Override
    public void deleteFrameBuffer(int frameBufferId, boolean hasEglContext) {
        GLRenderer.getGLRecycler().deleteFrameBuffer(frameBufferId, hasEglContext);
    }

    @Override
    public void deleteBuffer(int bufferId, boolean hasEglContext) {
        GLRenderer.getGLRecycler().deleteBuffer(bufferId, hasEglContext);
    }

    @Override
    public void deleteRenderBuffer(int bufferId, boolean hasEglContext) {
        GLRenderer.getGLRecycler().deleteRenderBuffer(bufferId, hasEglContext);
    }

    @Override
    public void deleteProgram(int programId, boolean hasEglContext) {
        GLRenderer.getGLRecycler().deleteProgram(programId, hasEglContext);
    }

    private Render createRender(String key) {
        if (Render.NONE.equals(key)) {
            return new NoneRender(this);
        } else if (Render.BLUR.equals(key)) {
            return new StaticBlurRender(this);
        }else if (Render.GRAY.equals(key)) {
            return new GrayRender(this);
        } else if (Render.FOG.equals(key)) {
            return new FogRender(this);
        } else if (Render.WATER.equals(key)) {
            return new WaterRender(this);
        } else if (Render.YESTERDAY.equals(key)) {
            return new YesteryearRender(this);
        } else if (Render.VIVID.equals(key)) {
            return new VividRender(this);
        }  else if (Render.SEVENTY.equals(key)) {
            return new SeventyRender(this);
        } else if (Render.FISHEYE.equals(key)) {
            return new FishEyeRender(this);
        } else if (Render.MOSAIC.equals(key)) {
            return new MosaicRender(this);
        } else if (Render.SKETCH.equals(key)) {
            return new SketchEffectRender(this);
        }
        return null;
    }
}
