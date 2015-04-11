package ingvar.android.processor.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public abstract class AggregatedTask<K, R> extends AbstractTask<K, R> {

    protected static final int DEFAULT_PARALLEL_THREADS = Math.max(15, Runtime.getRuntime().availableProcessors() + 1); //15 at least
    protected static final int DEFAULT_AGGREGATE_KEEP_ALIVE_TIME_SECONDS = 5 * 60;

    private List<ITask<K, R>> tasks;
    private int threadsCount;
    private int keepAliveTimeout;
    private Map<ITask, Exception> innerExceptions;

    public AggregatedTask(K key, Class<R> resultClass) {
        super(key, resultClass);
        this.tasks = new LinkedList<>();
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

    public List<ITask<K, R>> getTasks() {
        return Collections.unmodifiableList(tasks);
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

}
