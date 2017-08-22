package com.android.blureffect;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

public class MainActivity extends Activity {
    private SeekBar mSeekBar;
    private GLEffectView mEffectView;
    private int MAX_VALUE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEffectView = (GLEffectView)findViewById(R.id.gl_effect_view);
        mSeekBar = (SeekBar)findViewById(R.id.seek_bar);
        mSeekBar.setMax(MAX_VALUE);
        //SeekBar.setVisibility(View.GONE);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                float factor = (float)progress/MAX_VALUE;
                mEffectView.setLevel(factor);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
    }
}
