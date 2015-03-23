package ingvar.examples.view;

import android.os.Bundle;

import ingvar.android.processor.service.Processor;
import ingvar.examples.serice.ExampleService;
import roboguice.activity.RoboActivity;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
public class AbstractActivity extends RoboActivity {

    protected Processor processor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processor = new Processor(ExampleService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        processor.bind(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        processor.unbind(this);
    }

}
