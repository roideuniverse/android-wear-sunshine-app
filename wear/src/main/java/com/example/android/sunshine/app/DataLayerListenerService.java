package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        for (DataEvent event : events) {
            Uri uri = event.getDataItem().getUri();
            Log.d(TAG, "type=" + event.getType() + "::uri=" + uri);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        super.onMessageReceived(messageEvent);
        Log.d(TAG, "onMessageReceived");
    }
}
