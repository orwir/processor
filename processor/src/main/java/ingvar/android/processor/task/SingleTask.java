package ingvar.android.processor.task;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.source.ISource;

/**
 * Base implementation of single process task.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.18.
 *
 * @param <K> key class
 * @param <R> single item class
 * @param <S> source class
 */
public abstract class SingleTask<K, R, S extends ISource> extends AbstractTask<K, R> {

    private Class<? extends ISource> sourceType;
    private long cacheExpirationTime;
    private int retryCount;

    /**
     * Task without cashing results.
     * cacheExpirationTime is set as Time.ALWAYS_EXPIRED.
     * Note: result will not cached.
     *
     * @param key task key
     * @param resultClass result class
     * @param sourceType source type
     */
    public SingleTask(K key, Class resultClass, Class<? extends ISource> sourceType) {
        this(key, resultClass, sourceType, Time.ALWAYS_EXPIRED);
    }

    /**
     *
     * @param key task key
     * @param resultClass result class
     * @param sourceType source type
     * @param refresh if true as expiration time will be used 1 millis, otherwise {@link Time#ALWAYS_RETURNED}
     */
    public SingleTask(K key, Class resultClass, Class<? extends ISource> sourceType, boolean refresh) {
        this(key, resultClass, sourceType, refresh ? 1 : Time.ALWAYS_RETURNED);
    }

    /**
     * Note: if you set cacheExpirationTime as Time.ALWAYS_EXPIRED result of task will not cached.
     *
     * @param key task key
     * @param resultClass result class
     * @param sourceType source type
     * @param cacheExpirationTime through how many milliseconds result of task will be expired in the cache
     */
    public SingleTask(K key, Class resultClass, Class<? extends ISource> sourceType, long cacheExpirationTime) {
        super(key, resultClass);
        this.sourceType = sourceType;
        this.cacheExpirationTime = cacheExpirationTime;
        this.retryCount = 1; // 0 and 1 is same
    }

    /**
     * Do some process
     *
     * @param observerManager observer manager
     * @param source source
     * @return result of process
     */
    public abstract R process(IObserverManager observerManager, S source);

    /**
     * Return used source type
     *
     * @return source class
     */
    public Class<? extends ISource> getSourceType() {
        return sourceType;
    }

    /**
     * How much time cached result of the same task valid.
     *
     * @return time
     */
    public long getExpirationTime() {
        return cacheExpirationTime;
    }

    /**
     * Set expiration time
     *
     * @param cacheExpirationTime time
     */
    public void setExpirationTime(long cacheExpirationTime) {
        this.cacheExpirationTime = cacheExpirationTime;
    }

    /**
     * How many tries will be if process fails.
     *
     * @param tries
     */
    public void setRetryCount(int tries) {
        retryCount = tries;
    }

    /**
     * How many tries will be if process fails.
     *
     * @return tries
     */
    public int getRetryCount() {
        return retryCount;
    }

}
