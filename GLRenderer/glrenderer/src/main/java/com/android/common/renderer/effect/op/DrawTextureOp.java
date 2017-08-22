/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.op;

import com.android.common.renderer.effect.texture.Texture;

public class DrawTextureOp extends DrawOp {
    public Texture texture;

    public DrawTextureOp() {
    }

    public DrawTextureOp(Texture tex, int x, int y, int w, int h) {
        init(tex, x, y, w, h);
    }

    public DrawTextureOp init(Texture tex, int x, int y, int w, int h) {
        texture = tex;
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        return this;
    }

    public int getId() {
        return TEXTURE;
    }
}
