package com.example.android.sunshine.app;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import roideuniverse.sunshine.common.Constants;
import roideuniverse.sunshine.common.WeatherContract;

/**
 * Created by roide on 6/10/16.
 */
public class DataLayerListenerService extends WearableListenerService
{
    private static final String TAG = DataLayerListenerService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer)
    {
        super.onDataChanged(dataEventBuffer);
        Log.d(TAG, "onDataChanged::" + dataEventBuffer);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if(! connectionResult.isSuccess())
        {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        for(DataEvent event : events)
        {
            Uri uri = event.getDataItem().getUri();
            Log.d(TAG, "type=" + event.getType() + "::uri=" + uri);
            if(event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem() != null
                    && Constants.WEATHER_PATH.equals(event.getDataItem().getUri().getPath()))
            {
                Log.d(TAG, "Its a match - update local db");
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                DataMap data = dataMap.getDataMap(Constants.EXTRA_DATA_WEATHER);
                boolean isMetric = data.getBoolean(Constants.EXTRA_DATA_IS_METRIC);
                Log.d(TAG, "isMetric-" + isMetric);
                PrefManager.getInstance(getApplicationContext()).setMetric(isMetric);
                updateLocalDb(toCV(data));
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        super.onMessageReceived(messageEvent);
        Log.d(TAG, "onMessageReceived::Service::" + messageEvent);
    }

    private static ContentValues toCV(DataMap dataMap)
    {
        String key_loc = WeatherContract.WeatherEntry.COLUMN_LOC_KEY;
        String key_date = WeatherContract.WeatherEntry.COLUMN_DATE;
        String key_humidity = WeatherContract.WeatherEntry.COLUMN_HUMIDITY;
        String key_pressure = WeatherContract.WeatherEntry.COLUMN_PRESSURE;
        String key_wind_speed = WeatherContract.WeatherEntry.COLUMN_WIND_SPEED;
        String key_deg = WeatherContract.WeatherEntry.COLUMN_DEGREES;
        String key_max_tem = WeatherContract.WeatherEntry.COLUMN_MAX_TEMP;
        String key_min_temp = WeatherContract.WeatherEntry.COLUMN_MIN_TEMP;
        String key_desc = WeatherContract.WeatherEntry.COLUMN_SHORT_DESC;
        String key_weather_id = WeatherContract.WeatherEntry.COLUMN_WEATHER_ID;

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(key_loc, dataMap.getString(key_loc));
        weatherValues.put(key_date, dataMap.getString(key_date));
        weatherValues.put(key_humidity, dataMap.getString(key_humidity));
        weatherValues.put(key_pressure, dataMap.getString(key_pressure));
        weatherValues.put(key_wind_speed, dataMap.getString(key_wind_speed));
        weatherValues.put(key_deg, dataMap.getString(key_deg));
        weatherValues.put(key_max_tem, dataMap.getString(key_max_tem));
        weatherValues.put(key_min_temp, dataMap.getString(key_min_temp));
        weatherValues.put(key_desc, dataMap.getString(key_desc));
        weatherValues.put(key_weather_id, dataMap.getString(key_weather_id));
        return weatherValues;
    }

    private void updateLocalDb(ContentValues cvs)
    {
        Time dayTime = new Time();
        dayTime.setToNow();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        getApplicationContext().getContentResolver().insert(
                WeatherContract.WeatherEntry.CONTENT_URI, cvs);
        // delete old data so we don't build up an endless history
        getApplicationContext().getContentResolver().delete(WeatherContract.WeatherEntry
                        .CONTENT_URI,
                WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?",
                new String[]{Long.toString(dayTime.setJulianDay(julianStartDay - 1))});
    }
}
