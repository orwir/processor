package ingvar.android.processor.worker;

import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.exception.RequestCancelledException;
import ingvar.android.processor.exception.SourceNotAvailable;
import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.request.AggregatedRequest;
import ingvar.android.processor.request.IRequest;
import ingvar.android.processor.request.RequestStatus;
import ingvar.android.processor.request.SingleRequest;
import ingvar.android.processor.source.ISource;
import ingvar.android.processor.source.ISourceManager;

/**
 * Default implementation of async worker
 *
 * Created by Igor Zubenko on 2015.03.18.
 */
public class DefaultWorker implements IWorker {

    private static final String TAG = DefaultWorker.class.getSimpleName();

    protected final ExecutorService executorService;
    protected final ICacheManager cacheManager;
    protected final ISourceManager sourceManager;
    protected final IObserverManager observerManager;
    private final Map<IRequest, Future> executingRequests;

    public DefaultWorker(ExecutorService executorService, ICacheManager cacheManager, ISourceManager sourceManager, IObserverManager observerManager) {
        this.executorService = executorService;
        this.cacheManager = cacheManager;
        this.sourceManager = sourceManager;
        this.observerManager = observerManager;
        this.executingRequests = new ConcurrentHashMap<>();
    }

    @Override
    public <R> Future<R> execute(final IRequest request) {
        Callable callable = new Callable() {
            @Override
            public R call() throws Exception {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                return process(request);
            }
        };
        Future<R> future = executorService.submit(callable);
        executingRequests.put(request, future);
        return future;
    }

    @Override
    public <R> Future<R> getExecuted(IRequest request) {
        return executingRequests.get(request);
    }

    protected <R> R process(IRequest request) {
        request.setStatus(RequestStatus.PROCESSING);
        notifyProgress(request, IObserver.MIN_PROGRESS);
        checkCancellation(request);

        try {
            R result;
            if (request instanceof AggregatedRequest) {
                result = processAggregatedRequest((AggregatedRequest) request);
            }
            else if(request instanceof SingleRequest) {
                result = processSingleRequest((SingleRequest) request);
            }
            else {
                result = processCacheRequest(request);
            }

            checkCancellation(request);
            notifyProgress(request, IObserver.MAX_PROGRESS);
            notifyCompleted(request, result);
            return result;

        } catch (RequestCancelledException e) {
            notifyCancelled(request);
            return null;

        } catch (RuntimeException e) {
            Log.e(TAG, e.getMessage(), e);
            notifyFailed(request, e);
            throw e;

        } finally {
            executingRequests.remove(request);
        }
    }

    protected <R> R processAggregatedRequest(final AggregatedRequest request) {
        final ExecutorService innerExecutor = Executors.newFixedThreadPool(request.getThreadsCount());

        final AtomicInteger completed = new AtomicInteger(0);
        final List<IRequest> requests = request.getRequests();
        for(final IRequest inner : requests) {
            checkCancellation(request);

            innerExecutor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    checkCancellation(request);
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    Object innerResult = null;
                    try {
                        innerResult = process(inner);

                        int current = completed.incrementAndGet();
                        float progress = current * IObserver.MAX_PROGRESS / requests.size();
                        synchronized (request) {
                            checkCancellation(request);
                            notifyProgress(request, progress);
                            request.onRequestComplete(inner, innerResult);
                        }
                    } catch (RequestCancelledException e) {
                        //nothing to do
                    } catch (Exception e) {
                        synchronized (request) {
                            checkCancellation(request);
                            request.addRequestException(inner, e);
                        }
                        throw e;
                    }
                    return innerResult;
                }
            });
        }

        checkCancellation(request);
        innerExecutor.shutdown();
        try {
            boolean terminated = innerExecutor.awaitTermination(request.getKeepAliveTimeout(), TimeUnit.SECONDS);
            if(!terminated) {
                throw new ProcessorException("Process interrupted before all inner requests was handled.");
            }
        } catch (InterruptedException e) {
            throw new ProcessorException("Process interrupted before all inner requests was handled.");
        }

        return (R) request.getCumulativeResult();
    }

    protected <R> R processSingleRequest(SingleRequest request) {
        R result = null;

        flow: do { //for interrupting flow (loop executed only once)
            //start of flow ------------------------------------------------------------------------

            //try to get data from cache
            if (request.getExpirationTime() != Time.ALWAYS_EXPIRED) {
                request.setStatus(RequestStatus.LOADING_FROM_CACHE);

                checkCancellation(request);
                result = cacheManager.obtain(request.getRequestKey(), request.getResultClass(), request.getExpirationTime());
                if (result != null) {
                    break flow;
                }
            }

            //try to get from external source
            if(!sourceManager.isRegistered(request.getSourceType())) {
                throw new ProcessorException(String.format("Source type '%s' not registered", request.getSourceType()));
            }

            ISource source = sourceManager.getSource(request.getSourceType());
            if(source.isAvailable()) {
                request.setStatus(RequestStatus.LOADING_FROM_EXTERNAL);

                int tries = request.getRetryCount();
                RuntimeException exception = null;
                do {
                    checkCancellation(request);
                    try {
                        result = (R) request.loadFromExternalSource(observerManager, sourceManager.getSource(request.getSourceType()));
                        exception = null;
                    } catch (RuntimeException e) {
                        exception = e;
                    }
                } while (--tries > 0 && result == null);
                if(exception != null) {
                    throw exception;
                }
                if(result != null) {
                    cacheManager.put(request.getRequestKey(), result);
                    break flow;
                }
            } else {
                notifyFailed(request, new SourceNotAvailable(String.format("Source type '%s' is not available now", request.getSourceType())));
                break flow;
            }

            //end of flow --------------------------------------------------------------------------
        } while (false);

        return result;
    }

    protected <R> R processCacheRequest(IRequest request) {
        R result = null;
        request.setStatus(RequestStatus.LOADING_FROM_CACHE);

        checkCancellation(request);
        result = cacheManager.obtain(request.getRequestKey(), request.getResultClass(), Time.ALWAYS_RETURNED);
        return result;
    }

    protected void notifyProgress(IRequest request, float progress) {
        //adjust progress
        progress = Math.max(IObserver.MIN_PROGRESS, progress);
        progress = Math.min(IObserver.MAX_PROGRESS, progress);
        observerManager.notifyProgress(request, progress);
    }

    protected <R> void notifyCompleted(IRequest request, R result) {
        request.setStatus(RequestStatus.COMPLETED);
        observerManager.notifyCompleted(request, result);
    }

    protected void notifyCancelled(IRequest request) {
        request.setStatus(RequestStatus.COMPLETED);
        observerManager.notifyCancelled(request);
    }

    protected void notifyFailed(IRequest request, Exception e) {
        request.setStatus(RequestStatus.FAILED);
        observerManager.notifyFailed(request, e);
    }

    protected void checkCancellation(IRequest request) {
        if(request.isCancelled()) {
            throw new RequestCancelledException(String.format("Request '%s' was cancelled!", request.getRequestKey().toString()));
        }
    }

}

