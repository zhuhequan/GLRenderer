/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer.effect;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.android.common.renderer.GLRendererNotProguard;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class EffectUtils  {

    public static void view2Window(float m[], Rect src, Rect dst) {
        if (dst == null) return;
        float left  = src.left  + m[12];
        float top   = src.top + m[13];
        float right = src.right  + m[12];
        float bottom =src.bottom + m[13];
        dst.set((int)(left+0.5f), (int)(top+0.5f), (int)(right+0.5f), (int)(bottom+0.5f));
    }

    public static void window2View(float m[], float l, float t, float r, float b, Rect dst) {
        if (dst == null) return;
        float left  = l  - m[12];
        float top   = t - m[13];
        float right = r  - m[12];
        float bottom = b - m[13];
        dst.set((int)(left+0.5f), (int)(top+0.5f), (int)(right+0.5f), (int)(bottom+0.5f));
    }

    public static void glFillBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        if (!bitmap.isMutable()) {
            throw new IllegalArgumentException("Bitmap is not mutable.");
        }
        GLRenderer.loadLibraryIfNeeded();
        native_glReadPixels(bitmap, bitmap.getWidth(), bitmap.getHeight());
    }

    public static float[][] loadFromFileVertexOnly(String fname) {
        float[][] gledeInfo = new float[2][];

        ArrayList<Float> alv = new ArrayList<Float>();
        ArrayList<Integer> alFaceIndex = new ArrayList<Integer>();
        ArrayList<Float> alvResult = new ArrayList<Float>();
        ArrayList<Float> alt = new ArrayList<Float>();
        ArrayList<Float> altResult = new ArrayList<Float>();

        try {
            InputStream in = GLRenderer.getAppContext().
                    getResources().getAssets().open(fname);
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String temps = null;

            while ((temps = br.readLine()) != null) {
                String[] tempsa = temps.split("[ ]+");
                if (tempsa[0].trim().equals("v")) {
                    alv.add(Float.parseFloat(tempsa[1]));
                    alv.add(Float.parseFloat(tempsa[2]));
                    alv.add(Float.parseFloat(tempsa[3]));
                } else if (tempsa[0].trim().equals("vt")) {
                    alt.add(Float.parseFloat(tempsa[1]));
                    alt.add(Float.parseFloat(tempsa[2]));
                } else if (tempsa[0].trim().equals("f")) {
                    int[] index = new int[3];
                    index[0] = Integer.parseInt(tempsa[1].split("/")[0]) - 1;
                    float x0 = alv.get(3 * index[0]);
                    float y0 = alv.get(3 * index[0] + 1);
                    float z0 = alv.get(3 * index[0] + 2);
                    alvResult.add(x0);
                    alvResult.add(y0);
                    alvResult.add(z0);

                    index[1] = Integer.parseInt(tempsa[2].split("/")[0]) - 1;
                    float x1 = alv.get(3 * index[1]);
                    float y1 = alv.get(3 * index[1] + 1);
                    float z1 = alv.get(3 * index[1] + 2);
                    alvResult.add(x1);
                    alvResult.add(y1);
                    alvResult.add(z1);

                    index[2] = Integer.parseInt(tempsa[3].split("/")[0]) - 1;
                    float x2 = alv.get(3 * index[2]);
                    float y2 = alv.get(3 * index[2] + 1);
                    float z2 = alv.get(3 * index[2] + 2);
                    alvResult.add(x2);
                    alvResult.add(y2);
                    alvResult.add(z2);

                    alFaceIndex.add(index[0]);
                    alFaceIndex.add(index[1]);
                    alFaceIndex.add(index[2]);

                    int indexTex = Integer.parseInt(tempsa[1].split("/")[1]) - 1;
                    altResult.add(alt.get(indexTex * 2));
                    altResult.add(alt.get(indexTex * 2 + 1));

                    indexTex = Integer.parseInt(tempsa[2].split("/")[1]) - 1;
                    altResult.add(alt.get(indexTex * 2));
                    altResult.add(alt.get(indexTex * 2 + 1));

                    indexTex = Integer.parseInt(tempsa[3].split("/")[1]) - 1;
                    altResult.add(alt.get(indexTex * 2));
                    altResult.add(alt.get(indexTex * 2 + 1));
                }
            }
            int size = alvResult.size();
            float[] vXYZ = new float[size];
            for (int i = 0; i < size; i++) {
                vXYZ[i] = alvResult.get(i);
            }

            size = altResult.size();
            float[] tST = new float[size];
            for (int i = 0; i < size; i++) {
                if (i % 2 == 1) {
                    tST[i] = 1 - altResult.get(i);
                } else
                    tST[i] = altResult.get(i);
            }

            gledeInfo[0] = vXYZ;
            gledeInfo[1] = tST;

        } catch (Exception e) {
            Log.e("load error", "load error");
            e.printStackTrace();
        }
        return gledeInfo;
    }

    @GLRendererNotProguard
    private native static void native_glReadPixels(Object bitmap, int width, int height);
}
