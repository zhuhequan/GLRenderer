package com.android.common.renderer.effect;

public class SynchronizedIntArray {
    private static final int INIT_CAPACITY = 8;

    private int mData[] = new int[INIT_CAPACITY];
    private int mSize = 0;

    public synchronized void add(int value) {
        if (mData.length == mSize) {
            int temp[] = new int[mSize + mSize];
            System.arraycopy(mData, 0, temp, 0, mSize);
            mData = temp;
        }
        mData[mSize++] = value;
    }

    public synchronized int size() {
        return mSize;
    }

    public synchronized int[] toArray(int[] result) {
        if (result == null || result.length < mSize) {
            result = new int[mSize];
        }
        System.arraycopy(mData, 0, result, 0, mSize);
        return result;
    }

    public synchronized int[] getInternalArray() {
        return mData;
    }

    public synchronized void clear() {
        mSize = 0;
        if (mData.length != INIT_CAPACITY) mData = new int[INIT_CAPACITY];
    }
}
