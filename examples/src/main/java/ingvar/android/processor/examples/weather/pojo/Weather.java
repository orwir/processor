package ingvar.android.processor.examples.weather.pojo;

import java.io.Serializable;

import ingvar.android.literepo.conversion.annotation.Column;
import ingvar.android.literepo.conversion.annotation.Type;
import ingvar.android.processor.examples.weather.persistence.WeatherContract;

/**
 * Created by Igor Zubenko on 2015.03.24.
 */
public class Weather implements Serializable {

    @Column(value = WeatherContract.Weather.Col.CITY_ID, type = Type.INTEGER)
    private Integer cityId; //id
    @Column(value = WeatherContract.Weather.Col.RECEIVED_DATE, type = Type.INTEGER)
    private Long receivedDate; //dt
    @Column(value = WeatherContract.Weather.Col.NAME)
    private String name;

    //main block
    @Column(value = WeatherContract.Weather.Col.TEMP, type = Type.INTEGER)
    private Integer temp;
    @Column(value = WeatherContract.Weather.Col.HUMIDITY, type = Type.INTEGER)
    private Integer humidity;
    @Column(value = WeatherContract.Weather.Col.PRESSURE, type = Type.INTEGER)
    private Integer pressure;

    //wind block
    @Column(value = WeatherContract.Weather.Col.SPEED, type = Type.INTEGER)
    private Integer speed;
    @Column(value = WeatherContract.Weather.Col.DIRECTION, type = Type.INTEGER)
    private Integer direction; //deg
    @Column(value = WeatherContract.Weather.Col.GUST, type = Type.INTEGER)
    private Integer gust;
    @Column(value = WeatherContract.Weather.Col.CLOUDINESS, type = Type.INTEGER)
    private Integer cloudiness; //all

    //weather array
    @Column(value = WeatherContract.Weather.Col.DESCRIPTION)
    private String description;
    @Column(value = WeatherContract.Weather.Col.ICON)
    private String icon;

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Long getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Long receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTemp() {
        return temp;
    }

    public void setTemp(Integer temp) {
        this.temp = temp;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public Integer getGust() {
        return gust;
    }

    public void setGust(Integer gust) {
        this.gust = gust;
    }

    public Integer getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(Integer cloudiness) {
        this.cloudiness = cloudiness;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
