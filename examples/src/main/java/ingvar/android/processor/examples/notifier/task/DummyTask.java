package ingvar.android.processor.examples.notifier.task;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.source.ContextSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.07.09.
 */
public class DummyTask extends SingleTask<String, Integer, ContextSource> {

    static int count = 0;

    public DummyTask() {
        super("dummy", ContextSource.class);
    }

    @Override
    public Integer process(IObserverManager observerManager, ContextSource source) {
        return count++;
    }

}
