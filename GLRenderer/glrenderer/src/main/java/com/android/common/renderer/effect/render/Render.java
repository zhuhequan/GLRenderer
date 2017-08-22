/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.Resource;

abstract public class Render extends Resource {
    public static final String NONE = "__none";
    public static final String GRAY = "__gray";
    public static final String FOG = "__fog";
    public static final String VIVID = "__vivid";
    public static final String WATER = "__water";
    public static final String YESTERDAY = "__yesterday";
    public static final String SEVENTY = "__seventy";
    public static final String FISHEYE = "__fisheye";
    public static final String MOSAIC = "__mosaic";
    public static final String SKETCH = "__sketch";
    public static final String BLUR = "__static_blur";

    protected GLCanvas mGLCanvas;
    protected String mKey = NONE;

    public abstract boolean draw(DrawInfo drawInfo);

    public Render(GLCanvas canvas) {
        mGLCanvas = canvas;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {

    }

    public boolean valid() {
        return true;
    }
}
