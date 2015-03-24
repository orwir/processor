package ingvar.android.processor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.observation.ObserverManager;
import ingvar.android.processor.persistence.CacheManager;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.request.IRequest;
import ingvar.android.processor.source.ISourceManager;
import ingvar.android.processor.source.SourceManager;
import ingvar.android.processor.worker.DefaultWorker;
import ingvar.android.processor.worker.IWorker;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public abstract class ProcessorService extends Service {

    protected static final int DEFAULT_MIN_PARALLEL_THREADS = Runtime.getRuntime().availableProcessors() + 1;
    protected static final int DEFAULT_PARALLEL_THREADS = Math.max(4, DEFAULT_MIN_PARALLEL_THREADS); //4 at least
    protected static final int DEFAULT_KEEP_ALIVE_TIME_SECONDS = 5 * 60;

    public class ProcessorBinder extends Binder {

        public ProcessorService getService() {
            return ProcessorService.this;
        }

    }

    private ProcessorBinder binder;
    private ExecutorService executorService;
    private ICacheManager cacheManager;
    private ISourceManager sourceManager;
    private IObserverManager observerManager;
    private IWorker worker;

    public ProcessorService() {
        binder = new ProcessorBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        executorService = createExecutorService();
        cacheManager = createCacheManager();
        sourceManager = createSourceManager();
        observerManager = createObserverManager();
        worker = createWorker(executorService, cacheManager, sourceManager, observerManager);

        provideRepository(cacheManager);
        provideSources(sourceManager);
    }

    public <K, R> Future<R> execute(IRequest<K, R> request, IObserver<R>... observers) {
        for(IObserver observer : observers) {
            observerManager.add(request, observer);
        }

        Future<R> future = worker.getExecuted(request);
        if(future == null || !request.isMergeable()) {
            future = worker.execute(request);
        }

        return future;
    }

    public <K, R> R obtainFromCache(K key, Class dataClass, long expiryTime) {
        return cacheManager.obtain(key, dataClass, expiryTime);
    }

    public void removeObservers(String group) {
        observerManager.removeGroup(group);
    }

    public int getThreadCount() {
        return DEFAULT_PARALLEL_THREADS;
    }

    public int getAliveTime() {
        return DEFAULT_KEEP_ALIVE_TIME_SECONDS;
    }

    /**
     * Provide sources which is used in your application.
     *
     * @param sourceManager
     */
    protected abstract void provideSources(ISourceManager sourceManager);

    /**
     * Provide repositories which is used in your application.
     * Keep in mind what order of adding repositories to cache manager is important.
     * First matched repository will be used to persistence.
     *
     * @param cacheManager
     */
    protected abstract void provideRepository(ICacheManager cacheManager);

    protected ExecutorService createExecutorService() {
        return new ThreadPoolExecutor(
                DEFAULT_MIN_PARALLEL_THREADS, //initial pool size
                Math.max(getThreadCount(), DEFAULT_MIN_PARALLEL_THREADS), //max pool size
                getAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }

    protected ICacheManager createCacheManager() {
        return new CacheManager();
    }

    protected ISourceManager createSourceManager() {
        return new SourceManager();
    }

    protected IObserverManager createObserverManager() {
        return new ObserverManager();
    }

    protected IWorker createWorker(ExecutorService executorService, ICacheManager cacheManager, ISourceManager sourceManager, IObserverManager observerManager) {
        return new DefaultWorker(executorService, cacheManager, sourceManager, observerManager);
    }

}
