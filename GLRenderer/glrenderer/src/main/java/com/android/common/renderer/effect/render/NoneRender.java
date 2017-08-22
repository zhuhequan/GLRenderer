/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import com.android.common.renderer.effect.GLCanvas;

 public class NoneRender extends PixelsRender {
    public NoneRender(GLCanvas canvas) {
        super(canvas);
        mKey = NONE;
    }
 }
