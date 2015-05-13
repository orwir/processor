package ingvar.android.processor.task;

import java.util.UUID;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.util.CommonUtils;

/**
 * Base implementation of task.
 *
 * <br/><br/> Created by Igor Zubenko on 2015.04.11.
 *
 * @param <K> key class
 * @param <R> result class
 */
public abstract class AbstractTask<K, R> implements ITask<K, R> {

    private K key;
    private Class cacheClass;
    private boolean cancelled;
    private TaskStatus status;
    private boolean mergeable;
    private String uuid; //used if task is not mergeable

    public AbstractTask() {
        this(null, Void.class);
    }

    /**
     * @param key task identifier
     * @param cacheClass class used for getting appropriate cache-repository. If null will be used {@link Void}.
     */
    public AbstractTask(K key, Class cacheClass) {
        this.key = key;
        this.cacheClass = cacheClass == null ? Void.class : cacheClass;
        this.cancelled = false;
        this.status = TaskStatus.PENDING;
        setMergeable(key != null);
    }

    @Override
    public K getTaskKey() {
        return key;
    }

    @Override
    public Class getCacheClass() {
        return cacheClass;
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
        if(mergeable && key == null) {
            throw new ProcessorException("You can't set mergeable as true if you haven't key");
        }
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
        int hashCode = 31;
        hashCode += 31 * CommonUtils.objectHashCode(getTaskKey());
        hashCode += 31 * CommonUtils.objectHashCode(getCacheClass());
        hashCode += 31 * uuid.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AbstractTask) {
            AbstractTask o = (AbstractTask) obj;
            return CommonUtils.isEquals(uuid, o.uuid)
                && CommonUtils.isEquals(getTaskKey(), o.getTaskKey())
                && CommonUtils.isEquals(getCacheClass(), o.getCacheClass());
        }
        return false;
    }

    @Override
    public int compareTo(ITask<K, R> another) {
        return 0;
    }

    @Override
    public String toString() {
        return String.format("[class: %s, key: %s, uuid: %s]", getCacheClass().getSimpleName(), getTaskKey(), uuid);
    }

}
