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

    private static final String[] WEAR_WEATHER_COLUMNS = {
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
                WEAR_WEATHER_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        Log.d(TAG, "onLoadFinished::count=" + data.getCount());
        if(data.getCount() <=0 )
        {
            // Send msg to app that no data
            MessengerUtil.sendNoDataMessage(getApplicationContext());
            return;
        }
        for(int i=0;i<data.getCount(); i++)
        {
            data.moveToPosition(i);
            String date = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
            String maxTemp = data.getString(2);
            String minTemp = data.getString(3);
            String weatherId = data.getString(4);
            mMaxTempTv.setText(Util.formatTemperature(getApplicationContext(), Double.parseDouble(maxTemp)));
            mMinTempTv.setText(Util.formatTemperature(getApplicationContext(), Double.parseDouble(minTemp)));
            int iconRes = Util.getIconResourceForWeatherCondition(Integer.parseInt(weatherId));
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
}
