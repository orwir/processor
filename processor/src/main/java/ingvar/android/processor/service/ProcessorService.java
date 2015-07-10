package ingvar.android.processor.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.observation.ObserverManager;
import ingvar.android.processor.persistence.CacheManager;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.source.ISourceManager;
import ingvar.android.processor.source.SourceManager;
import ingvar.android.processor.task.AbstractTask;
import ingvar.android.processor.task.DefaultWorker;
import ingvar.android.processor.task.Execution;
import ingvar.android.processor.task.IWorker;
import ingvar.android.processor.task.ScheduledExecution;
import ingvar.android.processor.util.LW;

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
    private ScheduledExecutorService scheduledService;
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
        scheduledService = createScheduledService();
        cacheManager = createCacheManager();
        sourceManager = createSourceManager();
        observerManager = createObserverManager();
        worker = createWorker();

        provideRepositories(cacheManager);
        provideSources(sourceManager);
    }

    /**
     * Send task for execution.
     *
     * @param task task
     * @param observers task observers
     * @return {@link Execution}
     */
    public Execution execute(AbstractTask task, IObserver... observers) {
        for(IObserver observer : observers) {
            observerManager.add(task, observer);
        }

        Execution execution = worker.getExecuted(task);
        if(execution == null) {
            execution = worker.execute(task);
            LW.v(TAG, "Execute task %s", task);
        } else {
            LW.d(TAG, "Merged task %s", task);
        }

        return execution;
    }

    /**
     * Schedule task for single execution.
     * If task with same key & cache class already exists it will be cancelled and their observers will be removed.
     *
     * @param task task
     * @param delay the time from now to delay execution (millis)
     * @param observers task observers
     * @return {@link ScheduledExecution}
     */
    public ScheduledExecution schedule(AbstractTask task, long delay, IObserver... observers) {
        ScheduledExecution execution = worker.getScheduled(task);
        if(execution != null) {
            execution.cancel();
            observerManager.remove(task);
        }

        for(IObserver observer : observers) {
            observerManager.add(task, observer);
        }

        execution = worker.schedule(task, delay);
        LW.v(TAG, "Schedule task %s", task);

        return execution;
    }

    /**
     * Schedule task for multiple executions.
     * If task with same key & cache class already exists it will be cancelled and their observers will be removed.
     *
     * @param task task
     * @param initialDelay the time to delay first execution
     * @param delay the delay between the termination of one execution and the commencement of the next.
     * @param observers task observers
     * @return {@link ScheduledExecution}
     */
    public ScheduledExecution schedule(AbstractTask task, long initialDelay, long delay, IObserver... observers) {
        ScheduledExecution execution = worker.getScheduled(task);
        if(execution != null) {
            execution.cancel();
            observerManager.remove(task);
        }

        for(IObserver observer : observers) {
            observerManager.add(task, observer);
        }

        execution = worker.schedule(task, initialDelay, delay);
        LW.v(TAG, "Schedule task %s", task);

        return execution;
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
     * Executor for scheduling tasks
     *
     * @return executor
     */
    protected ScheduledExecutorService getScheduledService() {
        return scheduledService;
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
     * Create scheduled service.
     *
     * @return new scheduled execution service
     */
    protected ScheduledExecutorService createScheduledService() {
        return Executors.newScheduledThreadPool(DEFAULT_MIN_PARALLEL_THREADS);
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
     * @return new worker
     */
    protected IWorker createWorker() {
        return new DefaultWorker.Builder()
            .setExecutorService(getExecutorService())
            .setScheduledService(getScheduledService())
            .setCacheManager(getCacheManager())
            .setSourceManager(getSourceManager())
            .setObserverManager(getObserverManager())
        .build();
    }

}
