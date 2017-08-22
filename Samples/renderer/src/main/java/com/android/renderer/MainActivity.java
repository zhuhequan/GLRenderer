package com.android.renderer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.android.common.renderer.drawable.GLRendererDrawable;
import com.android.common.renderer.effect.GLRenderer;

public class MainActivity extends Activity {

    private View mRendererView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLRenderer.initialize(this);
        setContentView(R.layout.activity_main);
        mRendererView = findViewById(R.id.renderer_view);
        mRendererView.setBackground(new GLRendererDrawable(new GLES20TriangleRenderer(this)));
        mRendererView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRendererView.invalidate();
                if (!MainActivity.this.isDestroyed()) {
                    mRendererView.postDelayed(this, 16);
                }
            }
        }, 16);
    }
}
