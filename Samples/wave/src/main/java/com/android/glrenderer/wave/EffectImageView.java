package com.android.glrenderer.wave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.common.renderer.effect.GLRenderer;
import com.android.common.renderer.functor.DrawWaveBitmapFunctor;

public class EffectImageView extends ImageView {
    private DrawWaveBitmapFunctor mFunctor;
    private float mRatio = 0;
    public EffectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        GLRenderer.initialize(context);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (mFunctor == null) {
            mFunctor = new DrawWaveBitmapFunctor();
            mFunctor.setBitmap(((BitmapDrawable)getDrawable()).getBitmap());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mRatio += 0.2f;
        mFunctor.setProgress((int)mRatio);
        mFunctor.draw(canvas);
        invalidate();
    }
}
