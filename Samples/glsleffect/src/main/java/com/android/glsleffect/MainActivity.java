package com.android.glsleffect;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import com.android.common.renderer.drawable.GLSLDrawable;
import com.android.common.renderer.effect.GLRenderer;

public class MainActivity extends Activity {

    private View mGlslView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLRenderer.initialize(this);
        setContentView(R.layout.activity_main);
        mGlslView = findViewById(R.id.glsl_view);
        mGlslView.setBackground(new GLSLDrawable(sTestGlsl));
        mGlslView.postDelayed(new Runnable() {
            @Override
            public void run() {
                long time = SystemClock.uptimeMillis() % 200000L;
                float angleInDegrees = (180.0f / 200000.0f) * ((int)time);
                ((GLSLDrawable)mGlslView.getBackground()).setTime(angleInDegrees);
                mGlslView.invalidate();
                if (!MainActivity.this.isDestroyed()) {
                    mGlslView.postDelayed(this, 16);
                }
            }
        }, 16);
    }

    private static String sTestGlsl = "// By @paulofalcao\n" +
            "//\n" +
            "// Blobs\n" +
            "\n" +
            "#ifdef GL_ES\n" +
            "precision highp float;\n" +
            "#endif\n" +
            "\n" +
            "uniform float time;\n" +
            "uniform vec2 mouse;\n" +
            "uniform vec2 resolution;\n" +
            "uniform vec2 origin;\n" +
            "\n" +
            "float makePoint(float x,float y,float fx,float fy,float sx,float sy,float t){\n" +
            "   float xx=x+tan(t*fx)*sx;\n" +
            "   float yy=y+tan(t*fy)*sy;\n" +
            "   return 1.0/sqrt(xx*xx+yy*yy);\n" +
            "}\n" +
            "\n" +
            "void main( void ) {\n" +
            "\n" +
            "   vec2 p=((gl_FragCoord.xy - origin)/resolution.x)*2.0-vec2(1.0,resolution.y/resolution.x);\n" +
            "\n" +
            "   p=p*2.0;\n" +
            "   \n" +
            "   float x=p.x;\n" +
            "   float y=p.y;\n" +
            "\n" +
            "   float a=\n" +
            "       makePoint(x,y,3.3,2.9,0.3,0.3,time);\n" +
            "   a=a+makePoint(x,y,1.9,2.0,0.4,0.4,time);\n" +
            "   a=a+makePoint(x,y,0.8,0.7,0.4,0.5,time);\n" +
            "   a=a+makePoint(x,y,2.3,0.1,0.6,0.3,time);\n" +
            "   a=a+makePoint(x,y,0.8,1.7,0.5,0.4,time);\n" +
            "   a=a+makePoint(x,y,0.3,1.0,0.4,0.4,time);\n" +
            "   a=a+makePoint(x,y,1.4,1.7,0.4,0.5,time);\n" +
            "   a=a+makePoint(x,y,1.3,2.1,0.6,0.3,time);\n" +
            "   a=a+makePoint(x,y,1.8,1.7,0.5,0.4,time);   \n" +
            "   \n" +
            "   float b=\n" +
            "       makePoint(x,y,1.2,1.9,0.3,0.3,time);\n" +
            "   b=b+makePoint(x,y,0.7,2.7,0.4,0.4,time);\n" +
            "   b=b+makePoint(x,y,1.4,0.6,0.4,0.5,time);\n" +
            "   b=b+makePoint(x,y,2.6,0.4,0.6,0.3,time);\n" +
            "   b=b+makePoint(x,y,0.7,1.4,0.5,0.4,time);\n" +
            "   b=b+makePoint(x,y,0.7,1.7,0.4,0.4,time);\n" +
            "   b=b+makePoint(x,y,0.8,0.5,0.4,0.5,time);\n" +
            "   b=b+makePoint(x,y,1.4,0.9,0.6,0.3,time);\n" +
            "   b=b+makePoint(x,y,0.7,1.3,0.5,0.4,time);\n" +
            "\n" +
            "   float c=\n" +
            "       makePoint(x,y,3.7,0.3,0.3,0.3,time);\n" +
            "   c=c+makePoint(x,y,1.9,1.3,0.4,0.4,time);\n" +
            "   c=c+makePoint(x,y,0.8,0.9,0.4,0.5,time);\n" +
            "   c=c+makePoint(x,y,1.2,1.7,0.6,0.3,time);\n" +
            "   c=c+makePoint(x,y,0.3,0.6,0.5,0.4,time);\n" +
            "   c=c+makePoint(x,y,0.3,0.3,0.4,0.4,time);\n" +
            "   c=c+makePoint(x,y,1.4,0.8,0.4,0.5,time);\n" +
            "   c=c+makePoint(x,y,0.2,0.6,0.6,0.3,time);\n" +
            "   c=c+makePoint(x,y,1.3,0.5,0.5,0.4,time);\n" +
            "   \n" +
            "   vec3 d=vec3(a,b,c)/32.0;\n" +
            "   \n" +
            "   gl_FragColor = vec4(d.x,d.y,d.z,1.0);\n" +
            "}";
}
