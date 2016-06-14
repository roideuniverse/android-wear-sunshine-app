package roideuniverse.sunshine.common;

/**
 * Created by roide on 6/6/16.
 */
public class WeatherModel
{
    private String mLocationId;
    private String mDateTime;
    private String mHumidity;
    private String mPressure;
    private String mWindSpeed;
    private String mWindDirection;
    private String mHigh;
    private String mLow;
    private String mDescription;
    private String mWeatherId;

    public WeatherModel() {}

    public WeatherModel(String locationId, String dateTime, String humidity, String pressure,
                        String windSpeed, String windDirection, String high, String low,
                        String description, String weatherId)
    {
        mLocationId = locationId;
        mDateTime = dateTime;
        mHumidity = humidity;
        mPressure = pressure;
        mWindSpeed = windSpeed;
        mWindDirection = windDirection;
        mHigh = high;
        mLow = low;
        mDescription = description;
        mWeatherId = weatherId;
    }
}
