/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.functor;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.render.WaveEffectRender;

public class DrawWaveBitmapFunctor extends DrawBitmapFunctor {
    private int mProgress = 1;

    public void setProgress(int progress) {
        mProgress = progress;
    }

    public float setProgress() {
        return mProgress;
    }

    @Override
    public WaveEffectRender getRender(GLCanvas canvas) {
        WaveEffectRender render = (WaveEffectRender)canvas.getRender(WaveEffectRender.WAVE);
        if (render == null) {
            render = new WaveEffectRender(canvas);
            canvas.addRender(render);
        }
        render.setProgress(mProgress);
        return render;
    }
}
