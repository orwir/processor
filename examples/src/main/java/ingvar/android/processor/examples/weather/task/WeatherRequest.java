package ingvar.android.processor.examples.weather.task;

import java.util.concurrent.TimeUnit;

import ingvar.android.processor.examples.weather.network.RetrofitSource;
import ingvar.android.processor.examples.weather.pojo.Weather;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.03.24.
 */
public class WeatherRequest extends SingleTask<WeatherKey, Weather, RetrofitSource> {

    public WeatherRequest(WeatherKey key) {
        super(key, Weather.class, RetrofitSource.class, TimeUnit.HOURS.toMillis(1));
    }

    @Override
    public Weather process(IObserverManager observerManager, RetrofitSource source) {
        return source.getWeatherService().receiveWeather(getTaskKey().getCity());
    }

}
