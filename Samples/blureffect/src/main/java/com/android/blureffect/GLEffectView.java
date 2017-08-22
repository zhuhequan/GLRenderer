
package com.android.blureffect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;

import com.android.common.renderer.drawable.GLBlurBitmapDrawable;
import com.android.common.renderer.drawable.GLBlurDrawable;
import com.android.common.renderer.effect.GLRenderer;


public class GLEffectView extends View {
    private GLBlurDrawable mBlurDrawable;
    private GLBlurBitmapDrawable mBlurBitmapDrawable;
    public GLEffectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        GLRenderer.initialize(getContext());

//        //渐进模糊
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_full);
        mBlurBitmapDrawable = new GLBlurBitmapDrawable(bitmap,true);
        setBackground(mBlurBitmapDrawable);
        mBlurBitmapDrawable.setScale(0.4f);
        mBlurBitmapDrawable.setBlurLevel(0f);



//        //模糊bitmap
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_full);
//        Bitmap dst = Bitmap.createBitmap(bitmap.getWidth()/2,bitmap.getHeight()/2,bitmap.getConfig());
//        RendererUtils.blurBitmap(bitmap, dst, 20);
//        setBackground(new BitmapDrawable(dst));
//
//
//        mBlurDrawable = new GLBlurDrawable();
//        setBackground(mBlurDrawable);
//        mBlurDrawable.setBlurLevel(1f);
    }


    public void setLevel(float level) {
        if (mBlurDrawable != null) {
            mBlurDrawable.setBlurLevel(level);
        }
        if (mBlurBitmapDrawable != null) {
            mBlurBitmapDrawable.setBlurLevel(level);
        }
        invalidate();
    }
}
