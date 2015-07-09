package ingvar.android.processor.examples.notifier.task;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.source.ContextSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.07.09.
 */
public class DummyTask extends SingleTask<String, Void, ContextSource> {

    public DummyTask() {
        super("dummy", ContextSource.class);
    }

    @Override
    public Void process(IObserverManager observerManager, ContextSource source) {
        return null;
    }

}
