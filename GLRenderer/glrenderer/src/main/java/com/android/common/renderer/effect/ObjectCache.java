/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;


import java.util.ArrayList;
import java.util.Stack;

public class ObjectCache<T> {
    private int mMaxSize = 32;
    private Stack<T> mCache = new Stack<T>();
    private Class<? extends T> mClass;

    public ObjectCache(Class<? extends T> clazz, int maxSize) {
        mClass = clazz;
        mMaxSize = maxSize;
    }

    public T pop() {
        synchronized (this) {
            if (!mCache.isEmpty()) {
                return mCache.pop();
            }
            try {
                T o = mClass.newInstance();
                return o;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void put(T cache) {
        synchronized (this) {
            if (cache == null) return;
            for (int i = mCache.size() - 1; i >= 0; --i) {
                if (mCache.get(i) == cache) {
                    return;
                }
            }
            if (mCache.size() < mMaxSize) {
                mCache.push(cache);
            }
        }
    }

    public void put(ArrayList<T> caches) {
        synchronized (this) {
            for(T cache : caches) {
                if (mCache.size() >= mMaxSize) {
                    break;
                }
                put(cache);
            }
            caches.clear();
        }
    }

    public void clear() {
        synchronized (this) {
            mCache.clear();
        }
    }
}
