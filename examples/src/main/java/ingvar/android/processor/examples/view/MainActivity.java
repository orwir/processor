package ingvar.android.processor.examples.view;

import android.view.View;
import android.widget.Toast;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.dictionary.view.DictionaryActivity;
import ingvar.android.processor.examples.gallery.view.GalleryActivity;
import ingvar.android.processor.examples.notifier.view.NotifierActivity;
import ingvar.android.processor.examples.weather.view.WeatherActivity;
import roboguice.inject.ContentView;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends AbstractActivity {

    public void openExample(View view) {
        int id = view != null ? view.getId() : -1;

        switch (id) {
            case R.id.button_gallery:
                GalleryActivity.startActivity(this);
                break;
            case R.id.button_weather:
                WeatherActivity.startActivity(this);
                break;
            case R.id.button_dictionary:
                DictionaryActivity.startActivity(this);
                break;
            case R.id.button_notifier:
                NotifierActivity.startActivity(this);
                break;
            default:
                Toast.makeText(this, R.string.message_unknown_example, Toast.LENGTH_LONG).show();
                break;
        }
    }

}
