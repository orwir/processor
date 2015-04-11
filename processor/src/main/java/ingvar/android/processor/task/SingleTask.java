package ingvar.android.processor.task;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.source.ISource;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public abstract class SingleTask<K, R, S extends ISource> extends AbstractTask<K, R> {

    private Class<? extends ISource> sourceType;
    private long cacheExpirationTime;
    private int retryCount;

    public SingleTask(K key, Class<R> resultClass, Class<? extends ISource> sourceType, long cacheExpirationTime) {
        super(key, resultClass);
        this.sourceType = sourceType;
        this.cacheExpirationTime = cacheExpirationTime;
        this.retryCount = 1; // 0 and 1 is same
    }

    public abstract R process(IObserverManager observerManager, S source);

    public Class<? extends ISource> getSourceType() {
        return sourceType;
    }

    public void setRetryCount(int tries) {
        retryCount = tries;
    }

    public int getRetryCount() {
        return retryCount;
    }

}
