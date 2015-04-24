package ingvar.android.processor.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

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
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.source.ISourceManager;
import ingvar.android.processor.source.SourceManager;
import ingvar.android.processor.task.ITask;
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

    private static final String TAG = ProcessorService.class.getSimpleName();

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
            Log.d(TAG, "Executed new task with key '" + task.getTaskKey() + "'");
        } else {
            Log.d(TAG, "Returned future from previous task with the same key '" + task.getTaskKey() + "'");
        }

        return future;
    }

    /**
     * Obtain task result from cache.
     * Note: synchronous method.
     *
     * @param key result identifier
     * @param dataClass single result item class
     * @param expiryTime how much time data consider valid in the repository
     * @param <R> returned result class
     * @return cached result if exists and did not expired, null otherwise
     */
    public <R> R obtainFromCache(Object key, Class dataClass, long expiryTime) {
        return cacheManager.obtain(key, dataClass, expiryTime);
    }

    /**
     * Obtain task result from cache if exists.
     * Note: synchronous method.
     *
     * @param key result identifier
     * @param dataClass single result item class
     * @param <R> returned result class
     * @return cached result if exists, null otherwise
     */
    public <R> R obtainFromCache(Object key, Class dataClass) {
        return obtainFromCache(key, dataClass, Time.ALWAYS_RETURNED);
    }

    /**
     * Remove data from cache.
     * Note: synchronous method.
     *
     * @param dataClass data class
     * @param key data identifier
     * @param <K> identifier class
     */
    public <K> void removeFromCache(Class dataClass, K key) {
        cacheManager.remove(dataClass, key);
    }

    /**
     * Remove all data by data class.
     *
     * Note: synchronous method.
     *
     * @param dataClass data class.
     */
    public void removeFromCache(Class dataClass) {
        cacheManager.remove(dataClass);
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
     * Remove registered observers by group
     *
     * @param group group of observers
     */
    public void removeObservers(String group) {
        observerManager.removeGroup(group);
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
     * Get count of parallel threads.
     *
     * @return number of threads
     */
    public int getThreadCount() {
        return DEFAULT_PARALLEL_THREADS;
    }

    /**
     * Alive time of each thread.
     *
     * @return alive time
     */
    public int getAliveTime() {
        return DEFAULT_KEEP_ALIVE_TIME_SECONDS;
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
     * Executor for running threads.
     *
     * @return executor
     */
    protected ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Cache manager for caching task results.
     *
     * @return cache manager
     */
    protected ICacheManager getCacheManager() {
        return cacheManager;
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
     * Observer manager for notifications.
     *
     * @return observer manager
     */
    protected IObserverManager getObserverManager() {
        return observerManager;
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
