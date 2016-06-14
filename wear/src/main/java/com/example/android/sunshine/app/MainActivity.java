package com.example.android.sunshine.app;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import roideuniverse.sunshine.common.Constants;

public class MainActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        DataApi.DataListener, MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int FORECAST_LOADER = 0;
    private TextView mTextView;

    private GoogleApiClient mGoogleApiClient;

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
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
        Log.d(TAG, "OnCreate::");
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Log.d(TAG, "onCreateLoader");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        Log.d(TAG, "onLoadFinished");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        Log.d(TAG, "onLoaderReset");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer)
    {
        Log.d(TAG, "onDataChanged::");
        for (DataEvent event : dataEventBuffer) {
            Uri uri = event.getDataItem().getUri();
            Log.d(TAG, "type=" + event.getType() + "::uri=" + uri);
        }
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.d(TAG, "onConnected::" + bundle);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.d(TAG, "onConnectionSuspended::" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.d(TAG, "onConnectionFailed::" + connectionResult.getErrorCode());
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        Log.d(TAG, "onMessageReceived::" + messageEvent);
    }

    private class FetchWeatherAsyncTask extends AsyncTask<Uri, Void, Void>
    {
        private Context mContext;

        FetchWeatherAsyncTask(Context context)
        {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Uri... params)
        {
            // Connect to Play Services and the Wearable API
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Wearable.API)
                    .build();

            ConnectionResult connectionResult = googleApiClient.blockingConnect(
                    Constants.GOOGLE_API_CLIENT_TIMEOUT_S, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess() || !googleApiClient.isConnected()) {
                Log.e(TAG, String.format(Constants.GOOGLE_API_CLIENT_ERROR_MSG,
                        connectionResult.getErrorCode()));
                return null;
            }

            return null;
        }
    }
}
