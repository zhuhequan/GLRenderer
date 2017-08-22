package com.android.glede;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.common.renderer.effect.GLRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GledeActivity extends Activity  {
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLRenderer.initialize(this);
        setContentView(R.layout.main);
        mListView = (ListView)findViewById(R.id.list_view);
        List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < 1044; ++i) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("ICON", 0);
            contents.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, contents,
                                                 R.layout.list_item_layout,
                                                 new String[]{"ICON"},
                                                 new int[]{android.R.id.icon});
        mListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
