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
    private String uuid; //used if task is not mergeable
    private Execution execution;
    private boolean cancelFlag;

    public AbstractTask() {
        this(null, Void.class, true);
    }

    public AbstractTask(K key, Class cacheClass) {
        this(key, cacheClass, true);
    }

    /**
     * @param key task identifier
     * @param cacheClass class used for getting appropriate cache-repository. If null will be used {@link Void}.
     */
    public AbstractTask(K key, Class cacheClass, boolean mergeable) {
        this.key = key;
        this.cacheClass = cacheClass == null ? Void.class : cacheClass;
        setMergeable(key != null && mergeable);
        this.cancelFlag = false;
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
        cancelFlag = true;
        if(execution != null) {
            execution.cancel();
        }
    }

    @Override
    public boolean isCancelled() {
        boolean cancelled = cancelFlag;
        if(execution != null) {
            cancelled = execution.isCancelled();
        }
        return cancelled;
    }

    @Override
    public TaskStatus getStatus() {
        TaskStatus status = TaskStatus.PENDING;
        if(execution != null) {
            status = execution.getStatus();
        }
        return status;
    }

    @Override
    public boolean isMergeable() {
        return uuid.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public <E extends Execution> E getExecution() {
        return (E) execution;
    }

    public void setMergeable(boolean mergeable) {
        if(mergeable && key == null) {
            throw new ProcessorException("You can't set mergeable as true if you haven't key");
        }
        if(!TaskStatus.PENDING.equals(getStatus())) {
            throw new ProcessorException("Mergeable flag can be changed only in the pending task!");
        }
        uuid = mergeable ? "" : UUID.randomUUID().toString();
    }

    @Override
    public int hashCode() {
        int hashCode = 31;
        hashCode += 31 * CommonUtils.objectHashCode(getTaskKey());
        hashCode += 31 * CommonUtils.objectHashCode(getCacheClass());
        hashCode += 31 * CommonUtils.objectHashCode(uuid);
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

    void setStatus(TaskStatus status) {
        if(execution == null) {
            throw new IllegalStateException("Execution is null!");
        }
        execution.setStatus(status);
    }

    void setExecution(Execution execution) {
        this.execution = execution;
        if(cancelFlag) {
            execution.cancel();
        }
    }

}
