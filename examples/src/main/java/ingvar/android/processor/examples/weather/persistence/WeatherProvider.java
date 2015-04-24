package ingvar.android.processor.examples.weather.persistence;

import android.database.sqlite.SQLiteOpenHelper;

import ingvar.android.literepo.LiteProvider;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class WeatherProvider extends LiteProvider {

    @Override
    protected SQLiteOpenHelper provideOpenHelper() {
        return new WeatherHelper(getContext());
    }

}
