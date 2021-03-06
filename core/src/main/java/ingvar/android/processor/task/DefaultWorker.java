package ingvar.android.processor.task;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.exception.SourceNotAvailable;
import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.source.ISource;
import ingvar.android.processor.source.ISourceManager;
import ingvar.android.processor.util.LW;

/**
 * Default implementation of async worker.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.18.
 */
public class DefaultWorker implements IWorker {

    public static final String TAG = DefaultWorker.class.getSimpleName();

    public static class Builder {
        private ExecutorService executorService;
        private ScheduledExecutorService scheduledService;
        private ICacheManager cacheManager;
        private ISourceManager sourceManager;
        private IObserverManager observerManager;

        public Builder setExecutorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Builder setScheduledService(ScheduledExecutorService scheduledService) {
            this.scheduledService = scheduledService;
            return this;
        }

        public Builder setCacheManager(ICacheManager cacheManager) {
            this.cacheManager = cacheManager;
            return this;
        }

        public Builder setSourceManager(ISourceManager sourceManager) {
            this.sourceManager = sourceManager;
            return this;
        }

        public Builder setObserverManager(IObserverManager observerManager) {
            this.observerManager = observerManager;
            return this;
        }

        public DefaultWorker build() {
            if(executorService == null || scheduledService == null || cacheManager == null
            || sourceManager == null || observerManager == null) {
                throw new IllegalStateException("Can't create worker! Not all fields are filled!");
            }

            DefaultWorker worker = new DefaultWorker();
            worker.executorService = executorService;
            worker.scheduledService = scheduledService;
            worker.cacheManager = cacheManager;
            worker.sourceManager = sourceManager;
            worker.observerManager = observerManager;
            return worker;
        }

    }

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledService;
    private ICacheManager cacheManager;
    private ISourceManager sourceManager;
    private IObserverManager observerManager;
    protected final Map<ITask, Execution> executingTasks;
    protected final Map<ITask, ScheduledExecution> scheduledTasks;

    public DefaultWorker() {
        this.executingTasks = new ConcurrentHashMap<>();
        this.scheduledTasks = new ConcurrentHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Execution execute(AbstractTask task) {
        Execution execution = new Execution();
        task.setExecution(execution);
        Future future = executorService.submit(createCallableForExecution(task));
        execution.setFuture(future);
        executingTasks.put(task, execution);
        return execution;
    }

    @Override
    public ScheduledExecution schedule(AbstractTask task, long delay) {
        ScheduledExecution execution = new ScheduledExecution();
        task.setExecution(execution);
        execution.setDelay(delay);
        execution.setRepeatable(false);
        ScheduledFuture future = scheduledService.schedule(createCallableForExecution(task), delay, TimeUnit.MILLISECONDS);
        execution.setFuture(future);
        scheduledTasks.put(task, execution);
        return execution;
    }

    @Override
    public ScheduledExecution schedule(final AbstractTask task, long initialDelay, long delay) {
        ScheduledExecution execution = new ScheduledExecution();
        task.setExecution(execution);
        execution.setInitialDelay(initialDelay);
        execution.setDelay(delay);
        execution.setRepeatable(true);
        ScheduledFuture future = scheduledService.scheduleWithFixedDelay(createRunnableForExecution(task), initialDelay, delay, TimeUnit.MILLISECONDS);
        execution.setFuture(future);
        scheduledTasks.put(task, execution);
        return execution;
    }

    @Override
    public Execution getExecuted(AbstractTask task) {
        Execution execution = executingTasks.get(task);
        if(execution != null) {
            task.setExecution(execution);
        }
        return execution;
    }

    @Override
    public ScheduledExecution getScheduled(AbstractTask task) {
        ScheduledExecution execution = scheduledTasks.get(task);
        if(execution != null) {
            task.setExecution(execution);
        }
        return execution;
    }

    protected <R> R process(AbstractTask task) {
        try {
            task.setStatus(TaskStatus.STARTED);
            notifyProgress(task, IObserver.MIN_PROGRESS);

            checkCancellation(task);

            R result;
            if (task instanceof AggregatedTask) {
                result = processAggregatedTask((AggregatedTask) task);
            }
            else if(task instanceof SingleTask) {
                result = processSingleTask((SingleTask) task);
            }
            else {
                result = processTask(task);
            }

            checkCancellation(task);
            notifyProgress(task, IObserver.MAX_PROGRESS);
            notifyCompleted(task, result);
            LW.v(TAG, "Completed task %s", task);
            return result;

        } catch (CancellationException e) {
            LW.d(TAG, "Cancelled task %s", task);
            notifyCancelled(task);
            task.getExecution().getFuture().cancel(false);
            throw e;

        } catch (RuntimeException e) {
            LW.e(TAG, "Failed task %s", e, task);
            notifyFailed(task, e);
            throw e;

        } finally {
            executingTasks.remove(task);
            if(task.isCancelled()) {
                scheduledTasks.remove(task);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <R> R processAggregatedTask(final AggregatedTask aggregatedTask) {
        LW.v(TAG, "Process aggregated task %s, inners: %d", aggregatedTask, aggregatedTask.getTasks().size());

        final ExecutorService innerExecutor = Executors.newFixedThreadPool(aggregatedTask.getThreadsCount());

        final AtomicInteger completed = new AtomicInteger(0);
        final Set<AbstractTask> tasks = aggregatedTask.getTasks();
        for(final AbstractTask innerTask : tasks) {
            checkCancellation(aggregatedTask);

            Execution execution = new Execution();
            innerTask.setExecution(execution);
            Future future = innerExecutor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    checkCancellation(aggregatedTask);
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    LW.v(TAG, "Process inner task %s from AT %s", innerTask, aggregatedTask);
                    Object innerResult = null;
                    try {
                        innerResult = process(innerTask);

                        int current = completed.incrementAndGet();
                        float progress = current * IObserver.MAX_PROGRESS / tasks.size();
                        synchronized (aggregatedTask) {
                            checkCancellation(aggregatedTask);
                            notifyProgress(aggregatedTask, progress);
                            aggregatedTask.onTaskComplete(innerTask, innerResult);
                        }
                    } catch (CancellationException e) {
                        //nothing to do
                    } catch (Exception e) {
                        synchronized (aggregatedTask) {
                            checkCancellation(aggregatedTask);
                            aggregatedTask.addTaskException(innerTask, e);
                        }
                        throw e;
                    }
                    return innerResult;
                }
            });
            execution.setFuture(future);
            executingTasks.put(innerTask, execution);
        }

        checkCancellation(aggregatedTask);
        innerExecutor.shutdown();
        try {
            boolean terminated = innerExecutor.awaitTermination(aggregatedTask.getKeepAliveTimeout(), TimeUnit.SECONDS);
            if(!terminated) {
                throw new ProcessorException("Process interrupted before all inner requests was handled.");
            }
        } catch (InterruptedException e) {
            throw new ProcessorException("Process interrupted before all inner requests was handled.");
        }

        return (R) aggregatedTask.getCumulativeResult();
    }

    @SuppressWarnings("unchecked")
    protected <R> R processSingleTask(SingleTask task) {
        LW.v(TAG, "Process single task %s", task);

        R result = null;

        flow: do { //for interrupting flow (loop executed only once)
            //start of flow ------------------------------------------------------------------------

            //try to get data from cache
            if (canCache(task)) {
                task.setStatus(TaskStatus.LOADING_FROM_CACHE);

                checkCancellation(task);
                result = cacheManager.obtain(task.getTaskKey(), task.getCacheClass(), task.getExpirationTime());
                if (result != null) {
                    LW.v(TAG, "Task result got from cache %s", task);
                    break flow;
                }
            }

            //check is source type registered
            if(!sourceManager.isRegistered(task.getSourceType())) {
                throw new ProcessorException(String.format("Source type '%s' not registered", task.getSourceType()));
            }

            ISource source = sourceManager.getSource(task.getSourceType());
            if(source.isAvailable()) {
                task.setStatus(TaskStatus.PROCESSING);

                int tries = task.getRetryCount();
                RuntimeException exception;
                do {
                    checkCancellation(task);
                    try {
                        LW.v(TAG, "Try(%d) to process task %s", (task.getRetryCount() - tries), task);
                        result = (R) task.process(observerManager, source);
                        exception = null;
                    } catch (RuntimeException e) {
                        exception = e;
                    }
                } while (--tries > 0 && result == null);
                if(exception != null) {
                    throw exception;
                }
                if(result != null && canCache(task)) {
                    cacheManager.persist(task.getTaskKey(), task.getCacheClass(), result);
                    break flow;
                }
            } else {
                notifyFailed(task, new SourceNotAvailable(String.format("Source type '%s' is not available now", task.getSourceType())));
                break flow;
            }

            //end of flow --------------------------------------------------------------------------
        } while (false);

        return result;
    }

    protected <R> R processTask(AbstractTask task) {
        R result;
        task.setStatus(TaskStatus.LOADING_FROM_CACHE);

        checkCancellation(task);
        result = cacheManager.obtain(task.getTaskKey(), task.getCacheClass(), Time.ALWAYS_RETURNED);
        return result;
    }

    protected void notifyProgress(ITask task, float progress) {
        //adjust progress
        progress = Math.max(IObserver.MIN_PROGRESS, progress);
        progress = Math.min(IObserver.MAX_PROGRESS, progress);
        observerManager.notifyProgress(task, progress, null);
        LW.v(TAG, "Notify progress %.2f of task %s", progress, task);
    }

    protected <R> void notifyCompleted(AbstractTask task, R result) {
        task.setStatus(TaskStatus.COMPLETED);
        observerManager.notifyCompleted(task, result);
        LW.v(TAG, "Notify completed task %s", task);
    }

    protected void notifyCancelled(AbstractTask task) {
        task.setStatus(TaskStatus.CANCELLED);
        observerManager.notifyCancelled(task);
        LW.v(TAG, "Notify cancelled task %s", task);
    }

    protected void notifyFailed(AbstractTask task, Exception e) {
        task.setStatus(TaskStatus.FAILED);
        observerManager.notifyFailed(task, e);
        LW.v(TAG, "Notify failed '%s' task %s", e.getMessage(), task);
    }

    protected void checkCancellation(ITask task) {
        if(task.isCancelled()) {
            throw new CancellationException(String.format("Task '%s' was cancelled!", task.getTaskKey().toString()));
        }
    }

    protected boolean canCache(SingleTask task) {
        return Time.ALWAYS_EXPIRED != task.getExpirationTime()
            && !Void.class.equals(task.getCacheClass())
            && task.getTaskKey() != null;
    }

    protected <R> Callable createCallableForExecution(final AbstractTask task) {
        return new Callable() {
            @Override
            public R call() throws Exception {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                LW.d(TAG, "Process task %s", task);
                return process(task);
            }
        };
    }

    protected <R> Runnable createRunnableForExecution(final AbstractTask task) {
        return new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                LW.d(TAG, "Process task %s", task);
                process(task);
            }
        };
    }

    protected ExecutorService getExecutorService() {
        return executorService;
    }

    protected ScheduledExecutorService getScheduledService() {
        return scheduledService;
    }

    protected ICacheManager getCacheManager() {
        return cacheManager;
    }

    protected ISourceManager getSourceManager() {
        return sourceManager;
    }

    protected IObserverManager getObserverManager() {
        return observerManager;
    }

}

