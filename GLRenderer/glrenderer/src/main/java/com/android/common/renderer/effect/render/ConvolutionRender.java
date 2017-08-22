/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.GLCanvas;
abstract public class ConvolutionRender extends PixelsRender {
    protected int mUniformStepH;
    protected float mStepX;
    protected float mStepY;
    public ConvolutionRender(GLCanvas canvas) {
        super(canvas);
    }

    @Override
    protected void initProgram() {
        super.initProgram();
        mUniformStepH = GLES20.glGetUniformLocation(mProgram, "uStep");
    }
 }
