package xechwic.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import xechwic.android.act.MainApplication;

/**
 * 参数保存
 */

public class PrefsUtils {

    private  SharedPreferences sp;
    private static PrefsUtils mInstance;
     
    
    public static PrefsUtils getInstance(){
        if(mInstance==null){
            mInstance=new PrefsUtils();
        }
        return mInstance;
    }
  

    public PrefsUtils() {
        sp = MainApplication.getInstance().getSharedPreferences(MainApplication.getInstance().getPackageName(), Context.MODE_PRIVATE);
    }

    /**
     * *************** get ******************
     */

    public String get(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public boolean get(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public float get(String key, float defValue) {
        return sp.getFloat(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public long get(String key, long defValue) {
        return sp.getLong(key, defValue);
    }



   


    public void put(String key, String value) {
        if (value == null) {
            sp.edit().remove(key).apply();
        } else {
            sp.edit().putString(key, value).apply();
        }
    }

    public void put(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public void put(String key, float value) {
        sp.edit().putFloat(key, value).apply();
    }

    public void put(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public void putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public void clearAll() {
        sp.edit().clear().apply();
    }
}
