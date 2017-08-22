package com.android.common.renderer.effect.render;

import android.opengl.GLES20;

import com.android.common.renderer.effect.DrawInfo;
import com.android.common.renderer.effect.GLCanvas;
import com.android.common.renderer.effect.ShaderUtils;
import com.android.common.renderer.effect.op.DrawOp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by zhouxiang on 2016/11/2.
 */

public class GLSLRender extends Render {
    private static final float[] VERTICES = new float[]{-1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f};
    private static FloatBuffer VERTICES_BUFFER;

    private int mProgram = 0;
    private int mAPosition;

    private boolean mHasTime;
    private boolean mHasResolution;
    private boolean mHasOrigin;
    private boolean mHasParameter1;
    private boolean mHasParameter2;
    private boolean mHasParameter3;

    private int mUTimeHandle;
    private int mUResolutionHandle;
    private int mUOriginHandle;
    private int mUParameter1;
    private int mUParameter2;
    private int mUParameter3;

    private float mTime = 0;
    private float mParameter1 = 0;
    private float mParameter2 = 0;
    private float mParameter3 = 0;
    private float[] mResolution = new float[2];
    private float[] mOrigin = new float[2];

    private static final String VERTEX = "attribute vec3 position; void main() { gl_Position = vec4(position, 1.0); }";

    public GLSLRender(GLCanvas canvas, String glsl) {
        super(canvas);
        initProgram(glsl);
    }

    public boolean draw(DrawInfo drawInfo) {
        GLES20.glUseProgram(mProgram);
        initShader(drawInfo);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return true;
    }

    protected void initProgram(String glsl) {
        mProgram = ShaderUtils.createProgram(VERTEX, glsl);
        if (mProgram != 0) {
            GLES20.glUseProgram(mProgram);
            mAPosition = GLES20.glGetAttribLocation(mProgram, "position");

            mHasTime = glsl.contains("time");
            mHasResolution = glsl.contains("resolution");
            mHasOrigin = glsl.contains("origin");
            mHasParameter1 = glsl.contains("parameter1");
            mHasParameter2 = glsl.contains("parameter2");
            mHasParameter3 = glsl.contains("parameter3");

            if (mHasTime) {
                mUTimeHandle = GLES20.glGetUniformLocation(mProgram, "time");
            }
            if (mHasResolution) {
                mUResolutionHandle = GLES20.glGetUniformLocation(mProgram, "resolution");
            }
            if (mHasOrigin) {
                mUOriginHandle = GLES20.glGetUniformLocation(mProgram, "origin");
            }
            if (mHasParameter1) {
                mUParameter1 = GLES20.glGetUniformLocation(mProgram, "parameter1");
            }
            if (mHasParameter2) {
                mUParameter2 = GLES20.glGetUniformLocation(mProgram, "parameter2");
            }
            if (mHasParameter3) {
                mUParameter3 = GLES20.glGetUniformLocation(mProgram, "parameter3");
            }
        }
    }

    public void setTime(float time) {
        mTime = time;
    }

    public void setParameter1(float value) {
        mParameter1 = value;
    }
    public void setParameter2(float value) {
        mParameter2 = value;
    }
    public void setParameter3(float value) {
        mParameter3 = value;
    }

    public float getParameter1() {
        return mParameter1;
    }
    public float getParameter2() {
        return mParameter2;
    }
    public float getParameter3() {
        return mParameter3;
    }

    public float getTime() {
        return mTime;
    }

    protected void initShader(DrawInfo drawInfo) {
        GLES20.glVertexAttribPointer(mAPosition, 2, GLES20.GL_FLOAT, false, 8, getVertexBuffer());
        GLES20.glEnableVertexAttribArray(mAPosition);
        if (mHasTime) {
            GLES20.glUniform1f(mUTimeHandle, mTime);
        }
        if (mHasParameter1) {
            GLES20.glUniform1f(mUParameter1, mParameter1);
        }
        if (mHasParameter2) {
            GLES20.glUniform1f(mUParameter2, mParameter2);
        }
        if (mHasParameter3) {
            GLES20.glUniform1f(mUParameter3, mParameter3);
        }
        DrawOp e = drawInfo.drawOp;
        if (mHasResolution) {
            mResolution[0] = e.width;
            mResolution[1] = e.height;
            GLES20.glUniform2fv(mUResolutionHandle, 1, mResolution, 0);
        }
        if (mHasOrigin) {
            mOrigin[0] = e.x;
            mOrigin[1] = e.y;
            GLES20.glUniform2fv(mUOriginHandle, 1, mOrigin, 0);
        }
    }

    @Override
    public void trimResources(int level, boolean hasEglContext) {
        super.trimResources(level, hasEglContext);
        if (mProgram != 0 && mGLCanvas != null) {
            mGLCanvas.deleteProgram(mProgram, hasEglContext);
            mProgram = 0;
            mKey = null;
            mGLCanvas = null;
        }
    }

    @Override
    public boolean valid() {
        return mProgram != 0;
    }


    protected FloatBuffer getVertexBuffer() {
        return VERTICES_BUFFER;
    }

    public static ByteBuffer allocateByteBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    static {
        VERTICES_BUFFER = allocateByteBuffer(VERTICES.length * 32 / 8).asFloatBuffer();
        VERTICES_BUFFER.put(VERTICES);
        VERTICES_BUFFER.position(0);
    }
}

