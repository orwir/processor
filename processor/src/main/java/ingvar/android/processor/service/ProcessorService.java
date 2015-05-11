package ingvar.android.processor.service;

import android.app.Service;
import android.content.Context;
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
import ingvar.android.processor.source.ISourceManager;
import ingvar.android.processor.source.SourceManager;
import ingvar.android.processor.task.ITask;
import ingvar.android.processor.util.LW;
import ingvar.android.processor.worker.DefaultWorker;
import ingvar.android.processor.worker.IWorker;

/**
 * Service for async task execution.
 * Can cache result of task.
 * Can execute aggregated task(contains other tasks).
 * Receive and send notifications about task execution.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.18.
 */
public abstract class ProcessorService extends Service {

    public static final String TAG = ProcessorService.class.getSimpleName();

    /**
     * Default min count of parallel threads.
     */
    protected static final int DEFAULT_MIN_PARALLEL_THREADS = Runtime.getRuntime().availableProcessors() + 1;
    /**
     * Default count of parallel threads.
     */
    protected static final int DEFAULT_PARALLEL_THREADS = Math.max(4, DEFAULT_MIN_PARALLEL_THREADS); //4 at least
    /**
     * Default alive time of every task.
     */
    protected static final int DEFAULT_KEEP_ALIVE_TIME_SECONDS = 5 * 60;

    public class ProcessorBinder extends Binder {

        public ProcessorService getService() {
            return ProcessorService.this;
        }

    }

    private ExecutorService executorService;
    private ICacheManager cacheManager;
    private ISourceManager sourceManager;
    private IObserverManager observerManager;
    private IWorker worker;
    private final ProcessorBinder binder;

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

        provideRepositories(cacheManager);
        provideSources(sourceManager);
    }

    /**
     * Send task for execution.
     *
     * @param task task
     * @param observers task observers
     * @param <K> task identifier class
     * @param <R> task result class
     * @return {@link Future} of task execution
     */
    public <K, R> Future<R> execute(ITask<K, R> task, IObserver<R>... observers) {
        for(IObserver observer : observers) {
            observerManager.add(task, observer);
        }

        Future<R> future = worker.getExecuted(task);
        if(future == null || !task.isMergeable()) {
            future = worker.execute(task);
            LW.d(TAG, "Sent to execute new task %s", task);
        } else {
            LW.d(TAG, "Merged with previous task %s", task);
        }

        return future;
    }

    /**
     * Remove registered observers from context.
     * Used with {@link ingvar.android.processor.observation.ContextObserver}
     *
     * @param context context
     */
    public void removeObservers(Context context) {
        observerManager.removeGroup(context.getClass().getName());
    }

    /**
     * Remove all data from all repositories.
     *
     * Note: synchronous method.
     */
    public void clearCache() {
        cacheManager.remove();
    }

    /**
     * Cache manager for caching task results.
     *
     * @return cache manager
     */
    public ICacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * Observer manager for notifications.
     *
     * @return observer manager
     */
    public IObserverManager getObserverManager() {
        return observerManager;
    }

    /**
     * Provide sources which is used in your application.
     *
     * @param sourceManager source manager
     */
    protected abstract void provideSources(ISourceManager sourceManager);

    /**
     * Provide repositories which is used in your application.
     * Keep in mind what order of adding repositories to cache manager is important.
     * First matched repository will be used to persistence.
     *
     * @param cacheManager cache manager
     */
    protected abstract void provideRepositories(ICacheManager cacheManager);

    /**
     * Get count of parallel threads.
     *
     * @return number of threads
     */
    protected int getThreadCount() {
        return DEFAULT_PARALLEL_THREADS;
    }

    /**
     * Alive time of each thread.
     *
     * @return alive time
     */
    protected int getAliveTime() {
        return DEFAULT_KEEP_ALIVE_TIME_SECONDS;
    }

    /**
     * Executor for running threads.
     *
     * @return executor
     */
    protected ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Source manager for getting sources to tasks.
     *
     * @return source manager
     */
    protected ISourceManager getSourceManager() {
        return sourceManager;
    }

    /**
     * Worker for task processing.
     *
     * @return worker
     */
    protected IWorker getWorker() {
        return worker;
    }

    /**
     * Create executor service.
     *
     * @return new execution service
     */
    protected ExecutorService createExecutorService() {
        return new ThreadPoolExecutor(
                DEFAULT_MIN_PARALLEL_THREADS, //initial pool size
                Math.max(getThreadCount(), DEFAULT_MIN_PARALLEL_THREADS), //max pool size
                getAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }

    /**
     * Create cache manager
     *
     * @return new cache manager
     */
    protected ICacheManager createCacheManager() {
        return new CacheManager();
    }

    /**
     * Create source manager
     * @return new source manager
     */
    protected ISourceManager createSourceManager() {
        return new SourceManager();
    }

    /**
     * Create observer manager
     *
     * @return new observer manager
     */
    protected IObserverManager createObserverManager() {
        return new ObserverManager();
    }

    /**
     * Create worker
     *
     * @param executorService executor service
     * @param cacheManager cache manager
     * @param sourceManager source manager
     * @param observerManager observer manager
     * @return new worker
     */
    protected IWorker createWorker(ExecutorService executorService, ICacheManager cacheManager, ISourceManager sourceManager, IObserverManager observerManager) {
        return new DefaultWorker(executorService, cacheManager, sourceManager, observerManager);
    }

}
