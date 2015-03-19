package ingvar.android.processor.request;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.source.Source;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public abstract class SingleRequest<K, R, S extends Source> implements IRequest<K, R> {

    private K key;
    private Class<R> resultClass;
    private boolean cancelled;
    private RequestStatus status;
    private Class<? extends Source> sourceType;
    private long cacheExpirationTime;
    private int retryCount;
    private boolean mergeable;

    public SingleRequest(K key, Class<R> resultClass, Class<? extends Source> sourceType, long cacheExpirationTime) {
        this.key = key;
        this.resultClass = resultClass;
        this.sourceType = sourceType;
        this.cacheExpirationTime = cacheExpirationTime;

        this.cancelled = false;
        this.status = RequestStatus.PENDING;
        this.retryCount = 0;
        this.mergeable = true;
    }

    public abstract R loadFromExternalSource(IObserverManager observerManager, S source);

    public Class<? extends Source> getSourceType() {
        return sourceType;
    }

    @Override
    public K getRequestKey() {
        return key;
    }

    @Override
    public Class<R> getResultClass() {
        return resultClass;
    }

    public long getExpirationTime() {
        return cacheExpirationTime;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    @Override
    public RequestStatus getStatus() {
        return status;
    }

    public void setRetryCount(int tries) {
        retryCount = tries;
    }

    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public void setMergeable(boolean mergeable) {
        this.mergeable = mergeable;
    }

    @Override
    public boolean isMergeable() {
        return mergeable;
    }

    @Override
    public int hashCode() {
        int hashCode = 42;
        hashCode += 42 * getRequestKey().hashCode();
        hashCode += 42 * getResultClass().hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SingleRequest) {
            SingleRequest o = (SingleRequest) obj;
            return getRequestKey().equals(o.getRequestKey())
                    && getResultClass().equals(o.getResultClass())
                    && isMergeable() && o.isMergeable();
        }
        return false;
    }

}
