package com.example.android.sunshine.app;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import roideuniverse.sunshine.common.WeatherContract;

public class MainActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int FORECAST_LOADER = 0;
    private TextView mMaxTempTv;
    private TextView mMinTempTv;
    private ImageView mCurrWeatherImgView;

    private static final String[] WEATHER_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
        {
            @Override
            public void onLayoutInflated(WatchViewStub stub)
            {
                mMaxTempTv = (TextView) stub.findViewById(R.id.watch_temperature_max);
                mMinTempTv = (TextView) stub.findViewById(R.id.watch_temperature_min);
                mCurrWeatherImgView = (ImageView) stub.findViewById(R.id.watch_weather_img);
                getLoaderManager().initLoader(FORECAST_LOADER, null, MainActivity.this);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Log.d(TAG, "onCreateLoader");
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(MainActivity.this,
                WeatherContract.WeatherEntry.CONTENT_URI,
                WEATHER_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        Log.d(TAG, "onLoadFinished::count=" + data.getCount());
        for(int i=0;i<data.getCount(); i++)
        {
            data.moveToPosition(i);
            String date = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
            String maxTemp = data.getString(2);
            String minTemp = data.getString(3);
            String weatherId = data.getString(4);
            mMaxTempTv.setText(formatTemperature(getApplicationContext(), Double.parseDouble(maxTemp)));
            mMinTempTv.setText(formatTemperature(getApplicationContext(), Double.parseDouble(minTemp)));
            Log.d(TAG, "i=" + i + "::data=" + date + "::maxT=" + maxTemp + "::minT=" + minTemp + "::wid=" + weatherId);
            int iconRes = getIconResourceForWeatherCondition(Integer.parseInt(weatherId));
            if(iconRes != -1) {
                mCurrWeatherImgView.setImageResource(iconRes);
                mCurrWeatherImgView.setVisibility(View.VISIBLE);
            } else {
                mCurrWeatherImgView.setVisibility(View.GONE);
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        Log.d(TAG, "onLoaderReset");
    }

    public static String formatTemperature(Context context, double temperature)
    {
        // Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
        // the values here.
        String suffix = "\u00B0";
        Log.d(TAG, "isMetric=" + PrefManager.getInstance(context).isMetric());
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
