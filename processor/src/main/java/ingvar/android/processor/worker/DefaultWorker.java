package ingvar.android.processor.worker;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.exception.RequestCancelledException;
import ingvar.android.processor.observation.IProcessObserverManager;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.request.AggregatedRequest;
import ingvar.android.processor.request.IRequest;
import ingvar.android.processor.request.RequestStatus;
import ingvar.android.processor.request.SingleRequest;
import ingvar.android.processor.source.Source;
import ingvar.android.processor.source.SourceManager;

/**
 * Default implementation of async worker
 *
 * Created by Igor Zubenko on 2015.03.18.
 */
public class DefaultWorker implements IWorker {

    protected final ExecutorService executorService;
    protected final ICacheManager cacheManager;
    protected final SourceManager sourceManager;
    protected final IProcessObserverManager observerManager;

    public DefaultWorker(ExecutorService executorService, ICacheManager cacheManager, SourceManager sourceManager, IProcessObserverManager observerManager) {
        this.executorService = executorService;
        this.cacheManager = cacheManager;
        this.sourceManager = sourceManager;
        this.observerManager = observerManager;
    }

    @Override
    public <R> Future<R> execute(final IRequest request) {
        Callable callable = new Callable() {
            @Override
            public R call() throws Exception {
                return process(request);
            }
        };
        return executorService.submit(callable);
    }

    protected <R> R process(IRequest request) {
        request.setStatus(RequestStatus.PROCESSING);
        checkCancellation(request);

        R result = null;
        try {
            if (request instanceof AggregatedRequest) {
                result = processAggregatedRequest((AggregatedRequest) request);
            } else {
                result = processSingleRequest((SingleRequest) request);
            }

            checkCancellation(request);
            notifyCompleted(request, result);
            return result;

        } catch (RequestCancelledException e) {
            notifyCancelled(request);
            return null;

        } catch (RuntimeException e) {
            notifyFailed(request);
            throw e;
        }
    }

    protected <R> R processAggregatedRequest(final AggregatedRequest request) {
        final ExecutorService innerExecutor = Executors.newFixedThreadPool(request.getThreadsCount());

        final AtomicInteger completed = new AtomicInteger(0);
        final List<IRequest> requests = request.getRequests();
        for(final IRequest inner : requests) {
            checkCancellation(request);

            Future future = innerExecutor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    checkCancellation(request);
                    checkCancellation(inner);
                    Object innerResult = process(inner);

                    int current = completed.incrementAndGet();
                    float progress = current * 100f / requests.size();
                    synchronized (request) {
                        checkCancellation(request);
                        notifyProgress(request, progress);
                        request.onRequestComplete(inner, innerResult);
                    }
                    return innerResult;
                }
            });
        }
        //TODO: check throwing inner exception

        return (R) request.getCumulativeResult();
    }

    protected <R> R processSingleRequest(SingleRequest request) {
        R result = null;

        do { //for interrupting flow (loop executed only once)

            //try to get data from cache
            if (request.getExpirationTime() != Time.ALWAYS_EXPIRED) {
                request.setStatus(RequestStatus.LOADING_FROM_CACHE);

                checkCancellation(request);
                result = cacheManager.obtain(request.getRequestKey(), request.getExpirationTime());
                if (result != null) {
                    break;
                }
            }

            //try to get from external source
            if(!sourceManager.isRegistered(request.getSourceType())) {
                throw new ProcessorException(String.format("Source type '%s' not registered", request.getSourceType()));
            }

            Source source = sourceManager.get(request.getSourceType());
            if(source.isAvailable()) {
                request.setStatus(RequestStatus.LOADING_FROM_EXTERNAL);

                checkCancellation(request);
                try {
                    result = (R) request.loadFromExternalSource();
                } catch (RuntimeException e) {
                    //TODO: try to load if failed
                    throw e;
                }
                //put to cache before checking cancellation
                if(result != null) {
                    cacheManager.put(request.getRequestKey(), result);
                    break;
                }
            }

        } while (false);

        return result;
    }

    protected void notifyProgress(IRequest request, float progress) {
        //TODO:
    }

    protected <R> void notifyCompleted(IRequest request, R result) {
        request.setStatus(RequestStatus.COMPLETED);
        //TODO:
    }

    protected void notifyCancelled(IRequest request) {
        //TODO:
    }

    protected void notifyFailed(IRequest request) {
        request.setStatus(RequestStatus.FAILED);
        //TODO:
    }

    protected void checkCancellation(IRequest request) {
        if(request.isCancelled()) {
            throw new RequestCancelledException(String.format("Request '%s' was cancelled!", request.getRequestKey().toString()));
        }
    }

}

