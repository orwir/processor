package ingvar.android.processor.examples.weather.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ingvar.android.processor.examples.weather.pojo.Weather;
import ingvar.android.processor.network.source.NetworkSource;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by Igor Zubenko on 2015.03.24.
 */
public class RetrofitSource extends NetworkSource {

    private WeatherService weatherService;

    public RetrofitSource(Context context) {
        super(context);
        init();
    }

    public WeatherService getWeatherService() {
        return weatherService;
    }

    private final void init() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Weather.class, new WeatherDeserializer())
                .create();

        RestAdapter weatherAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.openweathermap.org")
                .setConverter(new GsonConverter(gson))
                .build();

        weatherService = weatherAdapter.create(WeatherService.class);
    }

}
