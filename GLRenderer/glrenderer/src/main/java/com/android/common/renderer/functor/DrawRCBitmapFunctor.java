/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.functor;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.render.RCEffectRender;

public class DrawRCBitmapFunctor extends DrawBitmapFunctor {
    private float mRadius = 12f;

    public void setRadius(float radius) {
        mRadius = radius;
    }

    public float getRadius() {
        return mRadius;
    }

    @Override
    public RCEffectRender getRender(GLCanvas canvas) {
        RCEffectRender render = (RCEffectRender)canvas.getRender(RCEffectRender.ROUND_CORNER);
        if (render == null) {
            render = new RCEffectRender(canvas);
            canvas.addRender(render);
        }
        render.setRadius(mRadius);
        return render;
    }
}
