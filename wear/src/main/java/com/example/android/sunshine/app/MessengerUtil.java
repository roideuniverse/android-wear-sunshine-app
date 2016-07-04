package com.example.android.sunshine.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by roide on 7/3/16.
 */
public final class MessengerUtil implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = MessengerUtil.class.getSimpleName();
    private static final String MSG_TEST_PATH = "/syncwear";
    private static final String MSG_DATA_EMPTY = "data-empty";
    private GoogleApiClient mGoogleApiClient;

    private MessengerUtil() {}

    public static void sendNoDataMessage(Context context)
    {
        MessengerUtil msg = new MessengerUtil();
        msg.sendMessage(context);
    }

    private void sendMessage(Context context)
    {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                        .await()
                        .getNodes();
                for(Node n : connectedNodes)
                {
                    sendMessage(n.getId(), MSG_DATA_EMPTY);
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Log.d(TAG, "connFailed:" + connectionResult);
    }

    private void sendMessage(String nodeId, String msg)
    {
        if(nodeId != null)
        {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, MSG_TEST_PATH, msg.getBytes())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>()
                    {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult)
                        {
                            if (sendMessageResult.getStatus().isSuccess())
                            {
                                Log.d(TAG, "sendMessageSuccess");
                            }
                            else
                            {
                                Log.d(TAG, "sendMessageFailed::" + sendMessageResult.getStatus().getStatusMessage());
                            }
                        }
                    });
        }
    }
}
