package ingvar.examples.weather.network;

import ingvar.examples.weather.pojo.Weather;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Igor Zubenko on 2015.03.24.
 */
public interface WeatherService {

    @GET("/data/2.5/weather?units=metric")
    Weather receiveWeather(@Query("q") String city);

}
