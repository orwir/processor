package ingvar.android.processor.filesystem.task;

import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class FilesystemTask<T> extends SingleTask<String, T, FilesystemSource> {

    public FilesystemTask(String key, Class<T> resultClass, long cacheExpirationTime) {
        super(key, resultClass, FilesystemSource.class, cacheExpirationTime);
    }

    @Override
    public T process(IObserverManager observerManager, FilesystemSource source) {
        return source.load(getTaskKey());
    }

}
