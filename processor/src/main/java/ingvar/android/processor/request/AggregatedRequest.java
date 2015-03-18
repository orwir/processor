package ingvar.android.processor.request;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public abstract class AggregatedRequest<K, R> implements IRequest<K, R> {

    protected static final int DEFAULT_THREADS_COUNT = 15;

    private List<IRequest<K, R>> requests;
    private K key;
    private Class<R> resultClass;
    private int threadsCount;

    public AggregatedRequest(K key, Class<R> resultClass) {
        this.key = key;
        this.resultClass = resultClass;
        this.requests = new LinkedList<>();
        this.threadsCount = DEFAULT_THREADS_COUNT;
    }

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

    public abstract void onRequestComplete(IRequest<K, R> completed, R result);

    public abstract R getCumulativeResult();

}
