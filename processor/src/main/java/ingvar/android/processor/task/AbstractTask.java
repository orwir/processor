package ingvar.android.processor.task;

import java.util.UUID;

import ingvar.android.processor.exception.ProcessorException;

/**
 * Base implementation of task.
 *
 * <br/><br/> Created by Igor Zubenko on 2015.04.11.
 *
 * @param <K> key class
 * @param <R> single result class
 */
public abstract class AbstractTask<K, R> implements ITask<K, R> {

    private K key;
    private Class<R> resultClass;
    private boolean cancelled;
    private TaskStatus status;
    private boolean mergeable;
    private String uuid; //used if task is not mergeable

    public AbstractTask(K key, Class<R> resultClass) {
        this.key = key;
        this.resultClass = resultClass;
        this.cancelled = false;
        this.status = TaskStatus.PENDING;
        setMergeable(true);
    }

    @Override
    public K getTaskKey() {
        return key;
    }

    @Override
    public Class<R> getResultClass() {
        return resultClass;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public void setMergeable(boolean mergeable) {
        if(!TaskStatus.PENDING.equals(status)) {
            throw new ProcessorException("Mergeable flag can be changed only in the pending task!");
        }
        this.mergeable = mergeable;
        uuid = mergeable ? "" : UUID.randomUUID().toString();
    }

    @Override
    public boolean isMergeable() {
        return mergeable;
    }

    @Override
    public int hashCode() {
        int hashCode = 42;
        hashCode += 42 * getTaskKey().hashCode();
        hashCode += 42 * getResultClass().hashCode();
        hashCode += 42 * uuid.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AbstractTask) {
            AbstractTask o = (AbstractTask) obj;
            return getTaskKey().equals(o.getTaskKey())
                    && getResultClass().equals(o.getResultClass())
                    && uuid.equals(o.uuid);
        }
        return false;
    }

    @Override
    public int compareTo(ITask<K, R> another) {
        return 0;
    }

    @Override
    public String toString() {
        return String.format("{'key': '%s', 'result': '%s'}", getTaskKey(), getResultClass().getSimpleName());
    }

}
