package ingvar.android.processor.examples.notifier.view;

import android.content.Context;

import ingvar.android.processor.observation.ContextObserver;

/**
 * Created by Igor Zubenko on 2015.07.09.
 */
public class ScheduledObserver extends ContextObserver<Context, Void> {

    public ScheduledObserver(Context context) {
        super(context);
    }

    @Override
    public void completed(Void result) {

    }

}
