package com.android.eglbitmap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.common.renderer.EGLBitmap;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView view = (ImageView) findViewById(R.id.view);
        view.setImageBitmap(getBitmap());
    }

    Bitmap getBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.ic_full, options);

        EGLBitmap eglbitmap = new EGLBitmap(options.outWidth, options.outHeight);
        Bitmap inBitmap = eglbitmap.getBitmap();
        options.inBitmap = inBitmap;
        options.inJustDecodeBounds = false;
        BitmapFactory.decodeResource(getResources(), R.drawable.ic_full, options);
        eglbitmap.release();
        return inBitmap;
    }
}
