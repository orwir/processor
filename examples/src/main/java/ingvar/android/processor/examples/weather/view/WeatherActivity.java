package ingvar.android.processor.examples.weather.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.view.AbstractActivity;
import ingvar.android.processor.examples.weather.pojo.Weather;
import ingvar.android.processor.examples.weather.task.WeatherRequest;
import ingvar.android.processor.observation.ActivityObserver;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created by Igor Zubenko on 2015.03.24.
 */
@ContentView(R.layout.activity_weather)
public class WeatherActivity extends AbstractActivity {

    @InjectView(R.id.weather_search)
    private SearchView search;
    @InjectView(R.id.weather_loading)
    private ProgressBar loading;

    @InjectView(R.id.weather_temp)
    private TextView temp;
    @InjectView(R.id.weather_humidity)
    private TextView humidity;
    @InjectView(R.id.weather_pressure)
    private TextView pressure;
    @InjectView(R.id.weather_description)
    private TextView description;
    @InjectView(R.id.weather_speed)
    private TextView speed;
    @InjectView(R.id.weather_direction)
    private TextView direction;
    @InjectView(R.id.weather_gust)
    private TextView gust;
    @InjectView(R.id.weather_cloudiness)
    private TextView cloudiness;


    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, WeatherActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        search.setIconifiedByDefault(false);
        clearFields();

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getProcessor().execute(new WeatherRequest(query), new WeatherObserver(WeatherActivity.this));
                setUIEnabled(false);
                clearFields();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setUIEnabled(boolean enabled) {
        search.setEnabled(enabled);
        loading.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    private void clearFields() {
        String na = getString(R.string.label_na);

        temp.setText(na);
        humidity.setText(na);
        pressure.setText(na);
        description.setText(na);
        speed.setText(na);
        direction.setText(na);
        gust.setText(na);
        cloudiness.setText(na);
    }

    private class WeatherObserver extends ActivityObserver<WeatherActivity, Weather> {

        public WeatherObserver(WeatherActivity activity) {
            super(activity);
        }

        @Override
        public void completed(Weather result) {
            WeatherActivity activity = getActivity();

            activity.setUIEnabled(true);
            activity.temp.setText(result.getTemp().toString() + "°C");
            activity.humidity.setText(result.getHumidity().toString() + "%");
            activity.pressure.setText(result.getPressure().toString() + " hPa");
            activity.description.setText(result.getDescription());
            activity.speed.setText(result.getSpeed().toString() + " mps");
            activity.direction.setText(result.getDirection().toString() + "°");
            if(result.getGust() != null) {
                activity.gust.setText(result.getGust().toString() + " mps");
            }
            activity.cloudiness.setText(result.getCloudiness().toString() + "%");
        }

        @Override
        public void cancelled() {
            WeatherActivity activity = getActivity();

            activity.setUIEnabled(true);
            Toast.makeText(activity, R.string.message_request_was_cancelled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void failed(Exception exception) {
            WeatherActivity activity = getActivity();

            activity.setUIEnabled(true);
            Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

}
