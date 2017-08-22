/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;
import com.android.common.renderer.effect.op.DrawOp;
import com.android.common.renderer.effect.render.Render;

public class DrawInfo {
    public boolean flipProjV;
    public boolean flipTextureV;
    public boolean flipTextureH;

    public boolean blend;
    public boolean depthTest;
    public boolean cullFace;
    public boolean clearFbo;

    public int alpha;

    public int viewportWidth;
    public int viewportHeight;
    public DrawOp drawOp;
    public String effectKey;

    public DrawInfo() {
        reset();
    }

    public void reset() {
        flipProjV = false;
        flipTextureV = false;
        flipTextureH = false;

        blend = false;
        depthTest = false;
        cullFace = false;
        clearFbo = false;

        alpha = 0xff;

        viewportWidth = 0;
        viewportHeight = 0;
        drawOp = null;
        effectKey = Render.NONE;
    }
};
