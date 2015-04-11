package ingvar.android.processor.task;

/**
 *
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface ITask<K, R> extends Comparable<ITask<K, R>> {

    /**
     * Return task key.
     *
     * @return unique identifier of request
     */
    K getTaskKey();

    /**
     * Return result class.
     *
     * @return class that represent the result
     */
    Class<R> getResultClass();

    /**
     * Notify worker what this task is cancelled.
     *
     * Trigger for method {@link ingvar.android.processor.observation.IObserver#cancelled()}
     */
    void cancel();

    /**
     * Check is task cancelled or not.
     *
     * @return true - if request cancelled, false otherwise
     */
    boolean isCancelled();

    /**
     *
     * @param status new status
     */
    void setStatus(TaskStatus status);

    /**
     *
     * @return current status
     */
    TaskStatus getStatus();

    /**
     * Can this tasked merged this prev task with the same task key {@link ITask#getTaskKey()}
     * @param mergeable
     */
    void setMergeable(boolean mergeable);

    /**
     *
     * @return is mergeable
     */
    boolean isMergeable();

}
