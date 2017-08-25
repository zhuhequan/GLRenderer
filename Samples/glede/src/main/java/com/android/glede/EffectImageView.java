package com.android.glede;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class EffectImageView extends ImageView {
    private DrawGledeFunctor mGledeFunctor;
    private float mPreviousY;
    private float mPreviousX;
    private float mAngleX = 0;
    private float mAngleY = 180;
    private float mRatio = 0;
    private int mOperator = 1;
    public EffectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGledeFunctor = new DrawGledeFunctor();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mAngleX = 0;
        mAngleY = 0;
        mPreviousX = 0;
        mPreviousY = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRatio = mRatio + mOperator * 0.02f;
        if (mRatio > 2.0f) {
            mRatio = 2.0f;
            mOperator = -mOperator;
        } else if (mRatio < 0) {
            mRatio = 0;
            mOperator = -mOperator;
        }
        mGledeFunctor.setAngleX(mAngleX);
        mGledeFunctor.setAngleY(mAngleY);
        mGledeFunctor.setRatio(mRatio);
        mGledeFunctor.draw(canvas);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float y = e.getY();
        float x = e.getX();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mAngleX += y - mPreviousY;
                mAngleY += x - mPreviousX;
                break;
        }
        mPreviousY = y;
        mPreviousX = x;
        return true;
    }
}
