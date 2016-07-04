package com.example.android.sunshine.app.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.android.sunshine.app.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import roideuniverse.sunshine.common.Constants;
import roideuniverse.sunshine.common.WeatherContract;

/**
 * Created by roide on 7/3/16.
 */
public class WearMessageListener extends WearableListenerService
{
    private static final String TAG = WearMessageListener.class.getSimpleName();
    private static final String[] WEAR_WEATHER_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,

            WeatherContract.WeatherEntry.COLUMN_LOC_KEY,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES

    };

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        if(messageEvent != null && messageEvent.getPath().equals("/syncwear"))
        {
            syncWear();
        }
    }

    private void syncWear()
    {
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Cursor c = getContentResolver().query(
                WeatherContract.WeatherEntry.CONTENT_URI,
                WEAR_WEATHER_COLUMNS,
                null,
                null,
                sortOrder);
        if(c!=null)
        {
            if(c.getCount() > 0)
            {
                c.moveToPosition(0);
                String date = c.getString(0);
                String shortDesc = c.getString(1);
                String maxTemp = c.getString(2);
                String minTemp = c.getString(3);
                String weatherId = c.getString(4);

                ContentValues cv = new ContentValues();
                cv.put(Constants.EXTRA_DATA_IS_METRIC, Utility.isMetric(getApplicationContext()));
                cv.put(WeatherContract.WeatherEntry.COLUMN_DATE, date);
                cv.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, maxTemp);
                cv.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, minTemp);
                cv.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, shortDesc);
                cv.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);
                cv.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, c.getString(5));
                cv.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, c.getString(6));
                cv.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, c.getString(7));
                cv.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, c.getString(8));
                cv.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, c.getString(9));
                sendDataToWear(getApplicationContext(), cv);
            }

        }
    }

    public static void sendDataToWear(Context context, ContentValues cv)
    {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult = googleApiClient.blockingConnect(
                Constants.GOOGLE_API_CLIENT_TIMEOUT_S, TimeUnit.SECONDS);

        DataMap data = new DataMap();
        boolean isMetric = Utility.isMetric(context);
        data.putBoolean(Constants.EXTRA_DATA_IS_METRIC, isMetric);
        data.putString(WeatherContract.WeatherEntry.COLUMN_DATE, cv.get(WeatherContract
                .WeatherEntry.COLUMN_DATE).toString());
        data.putString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, cv.get(WeatherContract
                .WeatherEntry.COLUMN_SHORT_DESC).toString());
        data.putString(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, cv.get(WeatherContract
                .WeatherEntry.COLUMN_MAX_TEMP).toString());
        data.putString(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, cv.get(WeatherContract
                .WeatherEntry.COLUMN_MIN_TEMP).toString());
        data.putString(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, cv.get(WeatherContract
                .WeatherEntry.COLUMN_WEATHER_ID).toString());

        data.putString(WeatherContract.WeatherEntry.COLUMN_LOC_KEY,
                cv.get(WeatherContract.WeatherEntry.COLUMN_LOC_KEY).toString());
        data.putString(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, cv.get(WeatherContract
                .WeatherEntry.COLUMN_HUMIDITY).toString());
        data.putString(WeatherContract.WeatherEntry.COLUMN_PRESSURE, cv.get(WeatherContract
                .WeatherEntry.COLUMN_PRESSURE).toString());
        data.putString(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, cv.get(WeatherContract
                .WeatherEntry.COLUMN_WIND_SPEED).toString());
        data.putString(WeatherContract.WeatherEntry.COLUMN_DEGREES, cv.get(WeatherContract
                .WeatherEntry.COLUMN_DEGREES).toString());

        if(connectionResult.isSuccess() && googleApiClient.isConnected())
        {

            PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.WEATHER_PATH);
            dataMap.getDataMap().putDataMap(Constants.EXTRA_DATA_WEATHER, data);
            dataMap.getDataMap().putLong(Constants.EXTRA_TIMESTAMP, new Date().getTime());
            PutDataRequest request = dataMap.asPutDataRequest();
            request.setUrgent();

            // Send the data over
            DataApi.DataItemResult result =
                    Wearable.DataApi.putDataItem(googleApiClient, request).await();

            if(! result.getStatus().isSuccess())
            {
                Log.e(TAG, String.format("Error sending data using DataApi (error code = %d)",
                        result.getStatus().getStatusCode()));
            }
            else
            {
                Log.d(TAG, "Data Updated successfully...");
            }

        }
        else
        {
            Log.e(TAG, String.format(Constants.GOOGLE_API_CLIENT_ERROR_MSG,
                    connectionResult.getErrorCode()));
        }
        googleApiClient.disconnect();
    }

}
