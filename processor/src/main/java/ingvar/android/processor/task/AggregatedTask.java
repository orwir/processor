package ingvar.android.processor.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Igor Zubenko on 2015.03.18.
 *
 * @param <K> key class
 * @param <R> single result class
 */
public abstract class AggregatedTask<K, R> extends AbstractTask<K, R> {

    protected static final int DEFAULT_PARALLEL_THREADS = Math.max(15, Runtime.getRuntime().availableProcessors() + 1); //15 at least
    protected static final int DEFAULT_AGGREGATE_KEEP_ALIVE_TIME_SECONDS = 5 * 60;

    private Set<ITask<K, R>> tasks;
    private int threadsCount;
    private int keepAliveTimeout;
    private Map<ITask, Exception> innerExceptions;

    public AggregatedTask(K key, Class resultClass) {
        super(key, resultClass);
        this.tasks = new HashSet<>();
        this.threadsCount = DEFAULT_PARALLEL_THREADS;
        this.keepAliveTimeout = DEFAULT_AGGREGATE_KEEP_ALIVE_TIME_SECONDS;
        this.innerExceptions = new HashMap<>();
    }

    public abstract void onTaskComplete(ITask<K, R> completed, R result);

    public abstract R getCumulativeResult();

    public void addTask(ITask<K, R> task) {
        tasks.add(task);
    }

    public void removeTask(ITask<K, R> task) {
        tasks.remove(task);
        innerExceptions.remove(task);
    }

    public Set<ITask<K, R>> getTasks() {
        return Collections.unmodifiableSet(tasks);
    }

    public int getThreadsCount() {
        return threadsCount;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public void addTaskException(ITask task, Exception exception) {
        innerExceptions.put(task, exception);
    }

    public Map<ITask, Exception> getTasksExceptions() {
        return Collections.unmodifiableMap(innerExceptions);
    }

    public boolean hasTasksExceptions() {
        return innerExceptions.size() > 0;
    }

}
