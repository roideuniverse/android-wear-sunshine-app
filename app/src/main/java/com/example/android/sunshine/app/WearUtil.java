package com.example.android.sunshine.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Set;

/**
 * Created by roide on 6/12/16.
 */
public class WearUtil
{
    private static final String TAG = WearUtil.class.getSimpleName();

    private GoogleApiClient mApiClient;

    public void sendMessage(Context context)
    {
        Log.d(TAG, "sendMessage");
        final String capability = context.getString(R.string.test_message_capability_name);
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
                {
                    @Override
                    public void onConnected(@Nullable Bundle bundle)
                    {
                        new AsyncTask<Void, Void, Void>()
                        {
                            @Override
                            protected Void doInBackground(Void... params)
                            {
                                List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await().getNodes();
                                for (Node n: connectedNodes)
                                {
                                    requestTranscription(n.getId(),"Hello World - yo kaushik");
                                }
                                return null;
                            }
                        }.execute();
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Log.d(TAG, "disconnecting");
                                mApiClient.disconnect();
                            }
                        }, 50000);
                    }

                    @Override
                    public void onConnectionSuspended(int i)
                    {
                        Log.d(TAG, "onConnectionSuspended::" + i);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener()
                {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Log.d(TAG, "onConnectionFailed::" + connectionResult.getErrorMessage());
                    }
                })
                .build();
        mApiClient.connect();
    }

    private static final String MSG_TEST_PATH = "/test";

    private void requestTranscription(String transcriptionNodeId, String msg) {
        Log.d(TAG, "requestTranscription::" + msg);
        if (transcriptionNodeId != null) {
            Wearable.MessageApi.sendMessage(mApiClient, transcriptionNodeId,
                    MSG_TEST_PATH, msg.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                // Failed to send message
                                Log.d(TAG, "Failed To Send Message");
                            } else {
                                Log.d(TAG, "Send message success");
                            }
                        }
                    }
            );
        } else {
            Log.d(TAG, "Unable to retrieve node with transcription capability");
        }
    }
}
