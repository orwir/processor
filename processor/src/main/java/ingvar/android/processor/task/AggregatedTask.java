package ingvar.android.processor.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Task which contains other tasks. Used for process several task as one and join their results.
 * Aggregated task does not cached, but inner tasks may be cached.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.18.
 *
 * @param <K> aggregated task key class
 * @param <G> aggregated result class
 * @param <I> inner task result class
 */
public abstract class AggregatedTask<K, G, I> extends AbstractTask<K, G> {

    /**
     * Default count of parallel threads for executing tasks which contains in the {@link AggregatedTask}
     */
    protected static final int DEFAULT_PARALLEL_THREADS = Math.max(15, Runtime.getRuntime().availableProcessors() + 1); //15 at least
    /**
     * Maximum time of execution whole {@link AggregatedTask}
     */
    protected static final int DEFAULT_AGGREGATE_KEEP_ALIVE_TIME_SECONDS = 5 * 60;

    private Set<ITask> tasks;
    private int threadsCount;
    private int keepAliveTimeout;
    private Map<ITask, Exception> innerExceptions;

    public AggregatedTask(Class resultClass) {
        this(null, resultClass);
    }

    /**
     *
     * @param key does not used in the inner processes, but may be used for you.
     * @param resultClass cumulative result class
     */
    public AggregatedTask(K key, Class resultClass) {
        super(key, resultClass);
        this.tasks = new HashSet<>();
        this.threadsCount = DEFAULT_PARALLEL_THREADS;
        this.keepAliveTimeout = DEFAULT_AGGREGATE_KEEP_ALIVE_TIME_SECONDS;
        this.innerExceptions = new HashMap<>();
    }

    /**
     * Called when one task completed.
     *
     * @param completed completed task
     * @param result task result
     */
    public abstract void onTaskComplete(ITask completed, I result);

    /**
     * Return cumulative result of tasks.
     *
     * @return result
     */
    public abstract G getCumulativeResult();

    /**
     * Add task.
     *
     * @param task task
     */
    public void addTask(ITask task) {
        tasks.add(task);
    }

    /**
     * Remove task.
     *
     * @param task task
     */
    public void removeTask(ITask task) {
        tasks.remove(task);
        innerExceptions.remove(task);
    }

    /**
     * Get tasks.
     *
     * @return tasks
     */
    public Set<ITask> getTasks() {
        return Collections.unmodifiableSet(tasks);
    }

    /**
     * Get count of parallel threads for executing inner tasks.
     *
     * @return count of parallel threads
     */
    public int getThreadsCount() {
        return threadsCount;
    }

    /**
     * Set count of parallel threads for executing inner tasks.
     *
     * @param threadsCount count of parallel threads
     */
    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
    }

    /**
     * Maximum time of execution this task.
     *
     * @return time
     */
    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    /**
     * Maximum time of execution this task.
     *
     * @param keepAliveTimeout time
     */
    public void setKeepAliveTimeout(int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    /**
     * Add exception of task task execution
     *
     * @param task failed task
     * @param exception exception
     */
    public void addTaskException(ITask task, Exception exception) {
        innerExceptions.put(task, exception);
    }

    /**
     * Get exceptions of inner tasks
     * @return
     */
    public Map<ITask, Exception> getTasksExceptions() {
        return Collections.unmodifiableMap(innerExceptions);
    }

    /**
     * Check if inner tasks failed.
     *
     * @return true if at least one task failed, false otherwise
     */
    public boolean hasTasksExceptions() {
        return innerExceptions.size() > 0;
    }

}
