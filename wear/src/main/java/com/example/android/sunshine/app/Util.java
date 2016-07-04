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

    public static int getIconResourceForWeatherCondition(int weatherId)
    {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if(weatherId >= 200 && weatherId <= 232)
        {
            return R.drawable.ic_storm;
        }
        else if(weatherId >= 300 && weatherId <= 321)
        {
            return R.drawable.ic_light_rain;
        }
        else if(weatherId >= 500 && weatherId <= 504)
        {
            return R.drawable.ic_rain;
        }
        else if(weatherId == 511)
        {
            return R.drawable.ic_snow;
        }
        else if(weatherId >= 520 && weatherId <= 531)
        {
            return R.drawable.ic_rain;
        }
        else if(weatherId >= 600 && weatherId <= 622)
        {
            return R.drawable.ic_snow;
        }
        else if(weatherId >= 701 && weatherId <= 761)
        {
            return R.drawable.ic_fog;
        }
        else if(weatherId == 761 || weatherId == 781)
        {
            return R.drawable.ic_storm;
        }
        else if(weatherId == 800)
        {
            return R.drawable.ic_clear;
        }
        else if(weatherId == 801)
        {
            return R.drawable.ic_light_clouds;
        }
        else if(weatherId >= 802 && weatherId <= 804)
        {
            return R.drawable.ic_cloudy;
        }
        return - 1;
    }
}
