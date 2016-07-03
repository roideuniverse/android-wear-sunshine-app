package roideuniverse.sunshine.common;

/**
 * Created by roide on 5/31/16.
 */
public class Constants
{
    public static int GOOGLE_API_CLIENT_TIMEOUT_S = 10;
    public static int MAX_WEATHERS = 20;

    public static final String GOOGLE_API_CLIENT_ERROR_MSG =
            "Failed to connect to GoogleApiClient (error code = %d)";

    public static String EXTRA_TIMESTAMP = "extra_timestamp";
    public static final String EXTRA_DATA_WEATHER = "extra_weathers";
    public static final String EXTRA_DATA_IS_METRIC = "extra_metric";

    public static final String WEATHER_PATH = "/weather";
}
