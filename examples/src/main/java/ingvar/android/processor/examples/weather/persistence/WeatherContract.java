package ingvar.android.processor.examples.weather.persistence;

import android.net.Uri;

import ingvar.android.processor.sqlite.persistence.ExtendedColumns;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class WeatherContract {

    public static final String AUTHORITY = "ingvar.android.examples.weather.provider";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY);

    public static class Weather {

        public static final String TABLE_NAME = "weather";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(URI, TABLE_NAME);
        public static final String[] PROJECTION = {Col._ID, Col._CREATION_DATE,
            Col.CITY_ID, Col.RECEIVED_DATE, Col.NAME, Col.TEMP, Col.HUMIDITY, Col.PRESSURE, Col.SPEED,
            Col.DIRECTION, Col.GUST, Col.CLOUDINESS, Col.DESCRIPTION, Col.ICON};

        public static class Col implements ExtendedColumns {
            public static final String CITY_ID = "city_id";
            public static final String RECEIVED_DATE = "received_date";
            public static final String NAME = "name";
            public static final String TEMP = "temp";
            public static final String HUMIDITY = "humidity";
            public static final String PRESSURE = "pressure";
            public static final String SPEED = "speed";
            public static final String DIRECTION = "direction";
            public static final String GUST = "gust";
            public static final String CLOUDINESS = "cloudiness";
            public static final String DESCRIPTION = "description";
            public static final String ICON = "icon";
        }

    }

    private WeatherContract() {}
}
