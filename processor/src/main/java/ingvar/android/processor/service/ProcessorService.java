package ingvar.android.processor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    protected static final int DEFAULT_PARALLEL_THREADS = 7;

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
    public boolean onUnbind(Intent intent) {
        boolean unbind = super.onUnbind(intent);
        String group = intent.getStringExtra(IObserver.KEY_GROUP);
        if(group != null && !group.isEmpty()) {
            observerManager.removeGroup(group);
        }
        return unbind;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        executorService = createExecutorService();
        cacheManager = createCacheManager();
        sourceManager = createSourceManager();
        observerManager = createObserverManager();
        worker = createWorker(executorService, cacheManager, sourceManager, observerManager);

        providePersisters(cacheManager);
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

    public int getThreadCount() {
        return DEFAULT_PARALLEL_THREADS;
    }

    protected abstract void provideSources(ISourceManager sourceManager);

    protected abstract void providePersisters(ICacheManager cacheManager);

    protected ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(getThreadCount());
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
