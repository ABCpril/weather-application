package mg.studio.weather.util;

/**
 * Created by 90904 on 2018/3/22 0022.
 */

public class TodayWeather {
    private String city;
    private String temperature;
    private String type;

    public String getCity() {
        return city;
    }
    public String getTemperature() {
        return temperature;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String toString() {
        return "TodayWeather{" +
                "city='" + city + '\'' +
                ", wendu='" + temperature + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
