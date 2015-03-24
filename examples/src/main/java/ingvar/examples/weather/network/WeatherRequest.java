package ingvar.examples.weather.network;

import java.util.concurrent.TimeUnit;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.request.SingleRequest;
import ingvar.examples.weather.pojo.Weather;

/**
 * Created by Igor Zubenko on 2015.03.24.
 */
public class WeatherRequest extends SingleRequest<String, Weather, RetrofitSource> {

    public WeatherRequest(String key) {
        super(key, Weather.class, RetrofitSource.class, TimeUnit.HOURS.toMillis(1));
    }

    @Override
    public Weather loadFromExternalSource(IObserverManager observerManager, RetrofitSource source) {
        return source.getWeatherService().receiveWeather(getRequestKey());
    }

}
