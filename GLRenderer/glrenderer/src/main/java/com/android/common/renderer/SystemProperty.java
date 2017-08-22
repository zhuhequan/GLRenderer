/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
package com.android.common.renderer;

import android.util.Log;

import java.lang.reflect.Method;

public class SystemProperty  {
    private static final String TAG = "glrenderer";
    private static Method get_Method = null;
    private static Method getBoolean_Method = null;
    private static Method getInt_Method = null;
    static {
        init();
    }

    public static String get(String key, String def) {
        String value = def;
        try {
            value = (String) get_Method.invoke(null, key);
        } catch (Exception e) {
            value = def;
            Log.e(TAG,"SystemProperty get "+e.toString());
        }
        return value;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static boolean getBoolean(String key, boolean def) {
        boolean value = def;
        try {
            value = (Boolean)getBoolean_Method.invoke(null, key, def);
        } catch (Exception e) {
            value = def;
            Log.e(TAG,"SystemProperty getBoolean "+e.toString());
        }
        return value;
    }

    public static int getInt(String key, int def) {
        int value = def;
        try {
            value = (Integer)getInt_Method.invoke(null, key, def);
        } catch (Exception e) {
            value = def;
            Log.e(TAG,"SystemProperty getBoolean "+e.toString());
        }
        return value;
    }

    private static void init() {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            get_Method = clazz.getMethod("get", String.class);
            getBoolean_Method = clazz.getMethod("getBoolean", String.class, boolean.class);
            getInt_Method =  clazz.getMethod("getInt", String.class, int.class);
        } catch (Exception e) {
            Log.e(TAG,"SystemProperty init "+e.toString());
        }
    }
}
