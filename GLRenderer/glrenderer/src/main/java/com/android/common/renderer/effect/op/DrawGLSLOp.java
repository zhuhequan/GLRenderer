/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.op;

public class DrawGLSLOp extends DrawOp {

    public DrawGLSLOp() {
    }

    public DrawGLSLOp(int x, int y, int w, int h) {
        init(x, y, w, h);
    }

    public DrawGLSLOp init(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        return this;
    }

    public int getId() {
        return GLSL;
    }
}
