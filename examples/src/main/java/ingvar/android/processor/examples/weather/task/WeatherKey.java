package ingvar.android.processor.examples.weather.task;

import android.net.Uri;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.processor.examples.weather.persistence.WeatherContract;
import ingvar.android.processor.sqlite.persistence.SqlKey;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class WeatherKey extends SqlKey {

    private String city;

    public WeatherKey(String city) {
        this.city = city;
        Uri uri = new UriBuilder()
        .authority(WeatherContract.AUTHORITY)
        .table(WeatherContract.Weather.TABLE_NAME)
        .where().eq(WeatherContract.Weather.Col.NAME, city).end()
        .build();
        setUri(uri);
        setProjection(WeatherContract.Weather.PROJECTION);
    }

    public String getCity() {
        return city;
    }

    @Override
    public String toString() {
        return city.toString();
    }
}
