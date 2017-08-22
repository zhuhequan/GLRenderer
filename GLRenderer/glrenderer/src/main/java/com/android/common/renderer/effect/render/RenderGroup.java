/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect.render;

import java.util.ArrayList;

import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.DrawInfo;


 public class RenderGroup extends Render {
    protected ArrayList<Render> mRenders = new ArrayList<Render>();
    public RenderGroup(GLCanvas canvas) {
        super(canvas);
    }

    public void addRender(Render render) {
        for (int i=0; i<mRenders.size(); ++i) {
            if (render.getKey().equals(mRenders.get(i).getKey())) {
                return;
            }
        }
        mRenders.add(render);
    }

    public Render getRender(String key) {
        for (int i=0; i<mRenders.size(); ++i) {
            if (key.equals(mRenders.get(i).getKey())) {
                return mRenders.get(i);
            }
        }
        return null;
    }

    @Override
    public boolean draw(DrawInfo drawInfo) {
        return false;
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        super.trimResources(level, hasEglContext);
        for (int i=0; i<mRenders.size(); ++i) {
            mRenders.get(i).trimResources(level, hasEglContext);
        }
        mRenders.clear();
    }
}
