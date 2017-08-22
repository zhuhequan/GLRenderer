/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import com.android.common.renderer.effect.render.Render;

public interface GLCanvas {
     public void draw(DrawInfo drawInfo);
     public StateMachine getState();
     public int getRootBindingFrameBuffer();

     public Render getRender(String key);
     public void addRender(Render render);

     public void deleteTexture(int texture, boolean hasEglContext);
     public void deleteFrameBuffer(int frameBufferId ,boolean hasEglContext);
     public void deleteBuffer(int bufferId, boolean hasEglContext);
     public void deleteRenderBuffer(int bufferId, boolean hasEglContext);
     public void deleteProgram(int programId, boolean hasEglContext);
}
