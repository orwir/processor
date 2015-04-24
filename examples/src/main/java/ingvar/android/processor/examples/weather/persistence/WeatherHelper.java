package ingvar.android.processor.examples.weather.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class WeatherHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather";
    public static final int DATABASE_VERSION = 1;

    public WeatherHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(createTableWeather());

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    private static final String createTableWeather() {
        String sql =
        "create table ${table_name} (" +
        "${id} integer primary key," +
        "${creation_date} integer default(strftime('%s', 'now') * 1000) not null," +
        "${city_id} integer not null," +
        "${received_date} integer not null," +
        "${city_name} text not null," +
        "${temp} integer not null," +
        "${humidity} integer," +
        "${pressure} integer," +
        "${speed} integer," +
        "${direction} integer," +
        "${gust} integer," +
        "${cloudiness} integer," +
        "${description} text," +
        "${icon} text" +
        ")";

        Map<String, String> replace = new HashMap<>();
        replace.put("table_name", WeatherContract.Weather.TABLE_NAME);
        replace.put("id", WeatherContract.Weather.Col._ID);
        replace.put("creation_date", WeatherContract.Weather.Col._CREATION_DATE);
        replace.put("city_id", WeatherContract.Weather.Col.CITY_ID);
        replace.put("received_date", WeatherContract.Weather.Col.RECEIVED_DATE);
        replace.put("city_name", WeatherContract.Weather.Col.NAME);
        replace.put("temp", WeatherContract.Weather.Col.TEMP);
        replace.put("humidity", WeatherContract.Weather.Col.HUMIDITY);
        replace.put("pressure", WeatherContract.Weather.Col.PRESSURE);
        replace.put("speed", WeatherContract.Weather.Col.SPEED);
        replace.put("direction", WeatherContract.Weather.Col.DIRECTION);
        replace.put("gust", WeatherContract.Weather.Col.GUST);
        replace.put("cloudiness", WeatherContract.Weather.Col.CLOUDINESS);
        replace.put("description", WeatherContract.Weather.Col.DESCRIPTION);
        replace.put("icon", WeatherContract.Weather.Col.ICON);

        return StrSubstitutor.replace(sql, replace);
    }

}
