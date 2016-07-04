package com.example.android.sunshine.app;

import android.content.Context;
import android.util.Log;

/**
 * Created by roide on 7/4/16.
 */
public class Util
{
    public static String formatTemperature(Context context, double temperature)
    {
        // Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
        // the values here.
        String suffix = "\u00B0";
        if(! PrefManager.getInstance(context).isMetric())
        {
            temperature = (temperature * 1.8) + 32;
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        return String.format(context.getString(R.string.format_temperature), temperature);
    }
}
