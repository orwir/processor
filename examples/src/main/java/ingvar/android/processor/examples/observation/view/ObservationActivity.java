package ingvar.android.processor.examples.observation.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.util.Map;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.view.AbstractActivity;
import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.source.ContextSource;
import ingvar.android.processor.task.AbstractTask;
import ingvar.android.processor.task.SingleTask;
import roboguice.inject.ContentView;

/**
 * Created by Igor Zubenko on 2015.07.11.
 */
@ContentView(R.layout.activity_observation)
public class ObservationActivity extends AbstractActivity {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ObservationActivity.class);
        context.startActivity(intent);
    }

    public void startTask(View view) {
        AbstractTask task = new ObservationTask(view.getId());
        if(view.getId() == R.id.button_cancelled) {
            task.cancel();
        }

        getProcessor().execute(task, new AbstractObserver() {
            @Override
            public String getGroup() {
                return ObservationActivity.class.getName();
            }

            @Override
            public void completed(Object result) {
                Toast.makeText(ObservationActivity.this, getString(R.string.message_task_completed), Toast.LENGTH_LONG).show();
            }

            @Override
            public void failed(Exception exception) {
                Toast.makeText(ObservationActivity.this, getString(R.string.message_task_failed), Toast.LENGTH_LONG).show();
            }

            @Override
            public void cancelled() {
                Toast.makeText(ObservationActivity.this, getString(R.string.message_task_cancelled), Toast.LENGTH_LONG).show();
            }

            @Override
            public void progress(float progress, Map extra) {
                Toast.makeText(ObservationActivity.this, getString(R.string.message_task_progress, progress), Toast.LENGTH_LONG).show();
            }

        });
    }

    class ObservationTask extends SingleTask<Integer, String, ContextSource> {

        public ObservationTask(Integer key) {
            super(key, ContextSource.class);
        }

        @Override
        public String process(IObserverManager observerManager, ContextSource source) {
            if(getTaskKey().equals(R.id.button_failed)) {
                throw new ProcessorException();
            }
            return null;
        }

    }

}
