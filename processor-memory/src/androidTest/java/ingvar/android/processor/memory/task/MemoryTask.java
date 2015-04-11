package ingvar.android.processor.memory.task;

import ingvar.android.processor.memory.source.MemorySource;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class MemoryTask extends SingleTask<String, Object, MemorySource> {

    public MemoryTask(String key, long cacheExpirationTime) {
        super(key, Object.class, MemorySource.class, cacheExpirationTime);
    }

    @Override
    public Object process(IObserverManager observerManager, MemorySource source) {
        return source.get(getTaskKey());
    }

}
