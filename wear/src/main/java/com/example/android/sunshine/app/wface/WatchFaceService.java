package com.example.android.sunshine.app.wface;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.example.android.sunshine.app.MessengerUtil;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import roideuniverse.sunshine.common.WeatherContract;

/**
 * Created by roide on 7/3/16.
 */
public class WatchFaceService extends CanvasWatchFaceService
{
    private static final String TAG = WatchFaceService.class.getSimpleName();
    private static final int MSG_UPDATE_TIME = 0;
    private static final int INTERACTIVE_UPDATE_RATE_MS = 1000 * 60;
    private static final String DATA_FORMAT = "EEE, MMM d, yyyy";

    private static final String[] WEAR_WEATHER_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    @Override
    public Engine onCreateEngine()
    {
        Log.d(TAG, "onCreateEngine");
        return new WatchFaceEngine();
    }

    private class WatchFaceEngine extends CanvasWatchFaceService.Engine implements Loader.OnLoadCompleteListener<Cursor>
    {
        final Handler mUpdateTimeHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch(msg.what)
                {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if(shouldTimerBeRunning())
                        {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                }
            }
        };

        // receiver to update the time zone
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        private int mBgColorInterActive = Color.parseColor("#2979FF");
        private int mBgColorDim = Color.BLACK;
        private int mWhiteInterActive = Color.WHITE;
        private int mWhiteAmbient = Color.parseColor("#50ffffff");

        private Calendar mCalendar;
        private SimpleDateFormat mSimpleDateFormat;

        private Paint mBackgroundPaint;
        private Paint mCurrTimePaint;
        private Paint mCurrDatePaint;
        private Paint mHorizontalLinePaint;
        private Paint mWeatherImagePaint;
        private Paint mMaxTemperaturePaint;
        private Paint mMinTemperaturePaint;

        private Rect mTempRect;

        private int mTempCenterX;
        private int mTempCenterY;

        private Resources mResources = getResources();

        private CursorLoader mCursorLoader;

        private boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder)
        {
            super.onCreate(holder);
            Log.d(TAG, "onCreate");

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mBgColorInterActive);

            mCalendar = Calendar.getInstance();
            mSimpleDateFormat = new SimpleDateFormat(DATA_FORMAT, Locale.US);

            mCurrTimePaint = createTextPaint(Color.WHITE,
                    mResources.getDimension(R.dimen.watch_time_text_size));

            mCurrDatePaint = createTextPaint(Color.parseColor("#60ffffff"),
                    mResources.getDimension(R.dimen.watch_date_text_size));

            mMaxTemperaturePaint = createTextPaint(Color.WHITE,
                    mResources.getDimension(R.dimen.watch_temp_text_size));

            mMinTemperaturePaint = createTextPaint(Color.parseColor("#60ffffff"),
                    mResources.getDimension(R.dimen.watch_temp_text_size));

            mHorizontalLinePaint = new Paint();
            mHorizontalLinePaint.setColor(Color.parseColor("#40ffffff"));

            mWeatherImagePaint = new Paint();

            // configure the system UI
            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle
                            .BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
            mCursorLoader = new CursorLoader(
                    getApplicationContext(),
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    WEAR_WEATHER_COLUMNS,
                    null,
                    null,
                    sortOrder);
            mCursorLoader.registerListener(100, this);
            mCursorLoader.startLoading();
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if(mCursorLoader != null)
            {
                mCursorLoader.unregisterListener(this);
                mCursorLoader.cancelLoadInBackground();
                mCursorLoader.stopLoading();
                mCursorLoader.reset();
            }
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets)
        {
            super.onApplyWindowInsets(insets);
            Log.d(TAG, "onApplyWindowInset");
            boolean isRound = insets.isRound();
        }

        @Override
        public void onPropertiesChanged(Bundle properties)
        {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            Log.d(TAG, "onPropertiesChanged::" + mLowBitAmbient);
        }

        @Override
        public void onTimeTick()
        {
            super.onTimeTick();
            Log.d(TAG, "onTimeTick");
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode)
        {
            super.onAmbientModeChanged(inAmbientMode);
            Log.d(TAG, "onAmbientModeChanged::" + mLowBitAmbient + "::mode=" + inAmbientMode);

            adjustPaintColorToCurrentMode(mBackgroundPaint, mBgColorInterActive, mBgColorDim);
            adjustPaintColorToCurrentMode(mCurrTimePaint, mWhiteInterActive, mWhiteAmbient);
            adjustPaintColorToCurrentMode(mMaxTemperaturePaint, mWhiteInterActive, mWhiteAmbient);

            if(mLowBitAmbient)
            {
                boolean antiAlias = !inAmbientMode;
                mHorizontalLinePaint.setAntiAlias(antiAlias);
                mBackgroundPaint.setAntiAlias(antiAlias);
                mCurrDatePaint.setAntiAlias(antiAlias);
                mCurrTimePaint.setAntiAlias(antiAlias);
                mMaxTemperaturePaint.setAntiAlias(antiAlias);
                mMinTemperaturePaint.setAntiAlias(antiAlias);
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds)
        {
            super.onDraw(canvas, bounds);
            Log.d(TAG, "onDraw");
            mTempCenterX = bounds.width()/2;
            mTempCenterY = bounds.height()/2;
            int stdMargin = mResources.getDimensionPixelSize(R.dimen.standard_margin);
            int halfMargin = mResources.getDimensionPixelSize(R.dimen.half_margin);
            int quarterMargin = mResources.getDimensionPixelSize(R.dimen.quarter_margin);
            mTempCenterY += halfMargin * 2;

            // Background
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            // Horizontal line
            canvas.drawLine(mTempCenterX - stdMargin, mTempCenterY, mTempCenterX + stdMargin, mTempCenterY, mHorizontalLinePaint);

            //date
            mTempCenterY -= halfMargin;
            String date = getFormattedDate();
            int dateHeight = getHeight(date, mCurrDatePaint);
            int dateWidth = getWidth(date, mCurrDatePaint);
            canvas.drawText(date, mTempCenterX - dateWidth/2, mTempCenterY - dateHeight/2, mCurrDatePaint);

            //time
            mTempCenterY -= halfMargin;
            String time = getFormattedTime();
            int timeHeight = getHeight(time, mCurrTimePaint);
            int timeWidth = getWidth(time, mCurrTimePaint);
            canvas.drawText(time, mTempCenterX - timeWidth/2, mTempCenterY - dateHeight - timeHeight/2, mCurrTimePaint);
            mTempCenterY += halfMargin;

            //Temp Min
            String tempMin = getTempMin();
            int tempMinHeight = getHeight(tempMin, mMaxTemperaturePaint);
            int tempMinWidth = getWidth(tempMin, mMaxTemperaturePaint);

            //Temp Max
            String tempMax = getTempMax();
            int tempMaxHeight = getHeight(tempMax, mMaxTemperaturePaint);
            int tempMaxWidth = getWidth(tempMax, mMaxTemperaturePaint);

            int tempWidth = tempMaxWidth + tempMinWidth;

            //Image
            mTempCenterY += halfMargin * 2;
            mTempCenterX -= halfMargin * 2;
            Drawable drawable = getWeatherStatusImage();
            int imgSize = mResources.getDimensionPixelSize(R.dimen.double_margin);
            int imgLeft = mTempCenterX - (imgSize + tempWidth) /2;
            int imgRight = mTempCenterX - (imgSize + tempWidth) /2 + imgSize;
            drawable.setBounds(imgLeft, mTempCenterY , imgRight, mTempCenterY + imgSize);
            if(isInAmbientMode())
            {
                drawable.setAlpha(64);
            }
            else
            {
                drawable.setAlpha(255);
            }
            drawable.draw(canvas);

            // Draw temp text
            canvas.drawText(tempMax, imgRight + halfMargin, mTempCenterY + tempMinHeight + tempMinHeight/2, mMaxTemperaturePaint);
            canvas.drawText(tempMin, imgRight + halfMargin*2 + tempMaxWidth, mTempCenterY + tempMaxHeight + tempMaxHeight/2, mMinTemperaturePaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible)
        {
            super.onVisibilityChanged(visible);
            Log.d(TAG, "onVisibilityChanged");
        }

        private boolean shouldTimerBeRunning()
        {
            return isVisible() && !isInAmbientMode();
        }

        private Paint createTextPaint(int color, float textSize)
        {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTextSize(textSize);
            paint.setAntiAlias(true);
            //paint.setTypeface();
            return paint;
        }

        private void adjustPaintColorToCurrentMode(Paint paint, int interactiveColor,
                                                   int ambientColor)
        {
            paint.setColor(isInAmbientMode() ? ambientColor : interactiveColor);
        }

        private String formatTwoDigitNumber(int number)
        {
            return String.format("%02d", number);
        }

        private String getFormattedDate()
        {
            return mSimpleDateFormat.format(mCalendar.getTime());
        }

        private String getFormattedTime()
        {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            String hour = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY));
            String minute = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
            return hour + ":" + minute;
        }

        private Drawable getWeatherStatusImage()
        {
            int res = Util.getIconResourceForWeatherCondition(mWeatherType);
            if(res != -1)
            {
                getResources().getDrawable(res);
            }
            return getResources().getDrawable(R.drawable.art_clear);
        }

        private String getTempMin()
        {
            return Util.formatTemperature(getApplicationContext(), Double.parseDouble(mMinTemp));
        }

        private String getTempMax()
        {
            return Util.formatTemperature(getApplicationContext(), Double.parseDouble(mMaxTemp));
        }

        private int getHeight(String text, Paint paint)
        {
            mTempRect = new Rect();
            paint.getTextBounds(text, 0, text.length(), mTempRect);
            return Math.abs(mTempRect.top - mTempRect.bottom);
        }

        private int getWidth(String text, Paint paint)
        {
            mTempRect = new Rect();
            paint.getTextBounds(text, 0, text.length(), mTempRect);
            return Math.abs(mTempRect.left - mTempRect.right);
        }

        @Override
        public void onLoadComplete(Loader<Cursor> loader, Cursor data)
        {
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
                mMaxTemp = maxTemp;
                mMinTemp = minTemp;
                mWeatherType = Integer.parseInt(weatherId);
                Log.d(TAG, "i=" + i + "::data=" + date + "::maxT=" + maxTemp + "::minT=" + minTemp + "::wid=" + weatherId);
            }
            invalidate();
        }

        private String mMinTemp = "0";
        private String mMaxTemp = "0";
        private int mWeatherType;
    }
}
