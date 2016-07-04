package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by roide on 7/3/16.
 */
public class PrefManager
{
    private static final String PREFERENCE_FILE = "app-preference";
    private static final String PREF_METRIC = "pref-metric";

    private SharedPreferences mSharedPreferences;
    private static PrefManager mInstance;

    public static PrefManager getInstance(Context context)
    {
        if(mInstance == null)
        {
            mInstance = new PrefManager(context);
        }
        return mInstance;
    }

    private PrefManager(Context context)
    {
        mSharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
    }

    public boolean isMetric()
    {
        return mSharedPreferences.getBoolean(PREF_METRIC, false);
    }

    public void setMetric(boolean isMetric)
    {
        mSharedPreferences.edit().putBoolean(PREF_METRIC, isMetric).apply();
    }
}
