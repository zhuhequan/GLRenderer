/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.op;

public abstract class DrawOp {
    public static final int TEXTURE = 0;
    public static final int GLSL = 1;

    //相对viewport原点位置
    public int x;
    public int y;
    public int width;
    public int height;

    //类型
    abstract public int getId();
}
