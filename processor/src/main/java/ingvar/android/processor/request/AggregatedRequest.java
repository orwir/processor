package ingvar.android.processor.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public abstract class AggregatedRequest<K, R> implements IRequest<K, R> {

    protected static final int DEFAULT_PARALLEL_THREADS = Math.max(15, Runtime.getRuntime().availableProcessors() + 1); //15 at least
    protected static final int DEFAULT_AGGREGATE_KEEP_ALIVE_TIME_SECONDS = 5 * 60;

    private K key;
    private Class<R> resultClass;
    private List<IRequest<K, R>> requests;
    private boolean cancelled;
    private RequestStatus status;
    private int threadsCount;
    private int keepAliveTimeout;
    private Map<IRequest, Exception> innerExceptions;

    public AggregatedRequest(K key, Class<R> resultClass) {
        this.key = key;
        this.resultClass = resultClass;

        this.cancelled = false;
        this.requests = new LinkedList<>();
        this.threadsCount = DEFAULT_PARALLEL_THREADS;
        this.keepAliveTimeout = DEFAULT_AGGREGATE_KEEP_ALIVE_TIME_SECONDS;
        this.innerExceptions = new HashMap<>();
    }

    public abstract void onRequestComplete(IRequest<K, R> completed, R result);

    public abstract R getCumulativeResult();

    @Override
    public K getRequestKey() {
        return key;
    }

    @Override
    public Class<R> getResultClass() {
        return resultClass;
    }

    public void addRequest(IRequest<K, R> request) {
        requests.add(request);
    }

    public void removeRequest(IRequest<K, R> request) {
        requests.remove(request);
        innerExceptions.remove(request);
    }

    public List<IRequest<K, R>> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    public int getThreadsCount() {
        return threadsCount;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public void addRequestException(IRequest request, Exception exception) {
        innerExceptions.put(request, exception);
    }

    public Map<IRequest, Exception> getExceptions() {
        return Collections.unmodifiableMap(innerExceptions);
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

    @Override
    public int hashCode() {
        int hashCode = 42;
        hashCode += 42 * getRequestKey().hashCode();
        hashCode += 42 * getResultClass().hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AggregatedRequest) {
            AggregatedRequest o = (AggregatedRequest) obj;
            return getRequestKey().equals(o.getRequestKey())
                    && getResultClass().equals(o.getResultClass())
                    && isMergeable() && o.isMergeable();
        }
        return false;
    }

    @Override
    public int compareTo(IRequest another) {
        return 0;
    }

}
