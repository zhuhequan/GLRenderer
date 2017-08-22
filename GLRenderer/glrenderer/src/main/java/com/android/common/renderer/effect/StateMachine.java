/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import android.opengl.Matrix;

import java.util.Stack;
import java.util.Vector;

public class StateMachine {
    private Stack<State> mStack = new Stack<State>();
    private  StateCache mStateCaches = new StateCache(25);
    private final float[] mMVPMatrix = new float[16];

    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float mTexMatrix[] =  new float[16];

    private int mFrameBufferId = 0;;

    public StateMachine() {
        reset();
    }

    private final float mIdentityMatrix[] = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1,
    };

    private static class State {
        final float[] mModelMatrix = new float[16];
        final float[] mTexMatrix = new float[16];
        int mFrameBufferId = 0;
        //final float[] mViewMatrix = new float[16];
        //float mAlpha = 1.0f;

        public State() {

        }

        public State init(float[] modelMatrix, float[] texMatrix,int frameBufferId) {
            System.arraycopy(modelMatrix, 0, mModelMatrix, 0, 16);
            System.arraycopy(texMatrix, 0, mTexMatrix, 0, 16);

            mFrameBufferId = frameBufferId;
            return this;
        }
    }

    private static class StateCache {
        private  int mSize = 25;
        private Vector<State> mCache = new Vector<State>();
        public StateCache(int size) {
            mSize = size;
        }

        public State get() {
            if (mCache.size() > 0) {
                return mCache.remove(mCache.size() - 1);
            }
            return new State();
        }

        public void put(State state) {
            if (state == null) return;
            for (int i=mCache.size()-1; i>=0; --i)  {
                if (mCache.get(i) == state) {
                    return;
                }
            }
            if (mCache.size() < mSize) {
                mCache.add(state);
            }
        }

        public void clear() {
            mCache.clear();
        }
    }

    public void identityAllM() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mTexMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjectionMatrix, 0);
    }

    public void identityModelM() {
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    public void identityTexM() {
        Matrix.setIdentityM(mTexMatrix, 0);
    }

    public void identityViewM() {
        Matrix.setIdentityM(mViewMatrix, 0);
    }

    public void identityProjectionM() {
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    public void push() {
        mStack.push(mStateCaches.get().init(mModelMatrix, mTexMatrix, mFrameBufferId));
    }

    public void pop() {
        State state = mStack.pop();
        if (state == null) throw new IllegalStateException("Wrong to invoke pop.");
        System.arraycopy(state.mModelMatrix, 0, mModelMatrix, 0, 16);
        System.arraycopy(state.mTexMatrix, 0, mTexMatrix, 0, 16);
        mFrameBufferId = state.mFrameBufferId;
        mStateCaches.put(state);
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(mModelMatrix, 0, x, y, z);
    }

    public void translate(float x, float y) {
        Matrix.translateM(mModelMatrix, 0, x, y, 0);
    }

    public void rotate(float angle, float x, float y, float z) {
        if (angle == 0) return;
        Matrix.rotateM(mModelMatrix, 0, angle, x, y, z);
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(mModelMatrix, 0, x, y, z);
    }

    public int getTranslateX() {

        return (int)mModelMatrix[12];
    }

    public int getTranslateY() {
        return (int)mModelMatrix[13];
    }

    public void posScale(float sx, float sy, float sz) {
         float scale[] = new float[] {
                sx,  0,   0,  0,
                0,   sy,  0,  0,
                0,   0,   sz, 0,
                0,   0,   0,  1,
        };
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, scale, 0, mModelMatrix, 0);
        System.arraycopy(result, 0, mModelMatrix, 0, 16);
    }

    public void multiplyMatrix(float matrix[], int offset) {
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, mModelMatrix, 0, matrix, offset);
        System.arraycopy(result, 0, mModelMatrix, 0, 16);
    }

    public void setMatrix(float matrix[], int offset) {
        System.arraycopy(matrix, offset, mModelMatrix, 0, 16);
    }

    public void setTexMatrix(float matrix[], int offset) {
        System.arraycopy(matrix, offset, mTexMatrix, 0, 16);
    }

    public void setLookAt(float cx, float cy, float cz, float tx,
            float ty, float tz, float upx, float upy, float upz) {
        Matrix.setLookAtM(mViewMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);
    }

    public void frustumM(float left, float right, float bottom, float top,float near,float far) {
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    public void orthoM(float left, float right, float bottom, float top) {
        Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, -100, 100);
    }

    public void setIdentity() {
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    public void setFrameBufferId(int id) {
        mFrameBufferId = id;
    }

    public int getFrameBufferId() {
        return mFrameBufferId;
    }

    /*
    public void setTexMatrix(float left, float top, float right, float bottom) {
        Matrix.setIdentityM(mTexMatrix, 0);
        mTexMatrix[0] = right - left;
        mTexMatrix[5] = bottom - top;
        mTexMatrix[10] = 1;
        mTexMatrix[12] = left;
        mTexMatrix[13] = top;
        mTexMatrix[15] = 1;
    }*/

    public float[] getFinalMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    public float[] getModelMatrix() {
        return mModelMatrix;
    }

    public float[] getIdentityMatrix() {
        return mIdentityMatrix;
    }

    public float[] getTexMaxtrix() {
        return mTexMatrix;
    }

    public void reset() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mTexMatrix, 0);
        mFrameBufferId = 0;
    }

    public static boolean isIndentity(float[] tranform) {
        for(int i = 0; i < 16; i++) {
            if (i%5 == 0) {
                if (tranform[i] != 1.0f) {
                    return false;
                }
            } else {
                if (tranform[i] != 0f) {
                    return false;
                }
            }
        }
        return true;
    }

    public void recycle() {
        mStateCaches.clear();
    }

}
