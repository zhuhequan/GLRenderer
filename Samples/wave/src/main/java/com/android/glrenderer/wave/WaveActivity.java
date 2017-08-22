package com.android.glrenderer.wave;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaveActivity extends Activity {
    private ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mListView = (ListView)findViewById(R.id.list_view);
        List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < 1044; ++i) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("ICON", R.drawable.linzhiling);
            contents.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, contents,
                R.layout.list_item_layout,
                new String[]{"ICON"},
                new int[]{android.R.id.icon});
        mListView.setAdapter(adapter);
    }
}
