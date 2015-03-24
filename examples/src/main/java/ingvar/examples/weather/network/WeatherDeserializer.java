package ingvar.examples.weather.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import ingvar.examples.weather.pojo.Weather;

/**
 * Created by Igor Zubenko on 2015.03.24.
 */
public class WeatherDeserializer implements JsonDeserializer<Weather> {

    @Override
    public Weather deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Weather weather = new Weather();

        JsonObject jWeather = json.getAsJsonObject();

        weather.setCityId(jWeather.get("id").getAsInt());
        weather.setReceivedDate(jWeather.get("dt").getAsLong());
        weather.setName(jWeather.get("name").getAsString());

        JsonObject jMain = jWeather.getAsJsonObject("main");
        weather.setTemp(jMain.get("temp").getAsInt());
        weather.setHumidity(jMain.get("humidity").getAsInt());
        weather.setPressure(jMain.get("pressure").getAsInt());

        JsonObject jWind = jWeather.getAsJsonObject("wind");
        weather.setSpeed(jWind.get("speed").getAsInt());
        weather.setDirection(jWind.get("deg").getAsInt());
        weather.setGust(jWind.get("gust").getAsInt());

        JsonObject jClouds = jWeather.getAsJsonObject("clouds");
        weather.setCloudiness(jClouds.get("all").getAsInt());

        JsonObject jAddition = jWeather.getAsJsonArray("weather").get(0).getAsJsonObject();
        weather.setDescription(jAddition.get("description").getAsString());
        weather.setIcon(jAddition.get("icon").getAsString());

        return weather;
    }

}
