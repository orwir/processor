package ingvar.android.processor.examples.notifier.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.notifier.task.DummyTask;
import ingvar.android.processor.examples.view.AbstractActivity;
import ingvar.android.processor.service.Processor;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created by Igor Zubenko on 2015.07.09.
 */
@ContentView(R.layout.activity_notifier)
public class NotifierActivity extends AbstractActivity {

    private static final long DEFAULT_DELAY = 10;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, NotifierActivity.class);
        context.startActivity(intent);
    }

    @InjectView(R.id.notifier_sec)
    private EditText mNotifierSecDelay;
    private DummyTask mTask;

    public void schedule(View view) {
        long delay = DEFAULT_DELAY;
        String sDelay = mNotifierSecDelay.getText().toString();
        if(!sDelay.isEmpty()) {
            delay = Long.valueOf(sDelay);
        }
        delay *= 1000;

        if(mTask != null) {
            Toast.makeText(this, R.string.message_previous_task_will_be_cancelled, Toast.LENGTH_LONG).show();
        }

        Processor p = getProcessor();
        if (p.isBound()) {
            mTask = new DummyTask();
            p.schedule(mTask, delay, delay, new NotifierObserver());
            Toast.makeText(this, getString(R.string.message_task_scheduled, delay / 1000), Toast.LENGTH_LONG).show();
        }
    }

    public void cancel(View view) {
        if(mTask == null) { //if activity was re-created
            DummyTask tmp = new DummyTask();
            if(getProcessor().getScheduled(tmp) != null) {
                tmp.cancel();
            }
        } else {
            mTask.cancel();
            mTask = null;
        }
    }

}
