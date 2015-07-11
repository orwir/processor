package ingvar.android.processor.task;

/**
 * Created by Igor Zubenko on 2015.03.18.
 *
 * @param <K> key class
 * @param <R> single value class
 */
public interface ITask<K, R> extends Comparable<ITask<K, R>> {

    /**
     * Return task key.
     *
     * @return unique identifier of request
     */
    K getTaskKey();

    /**
     * Return class used for getting appropriate cache-repository.
     *
     * @return class that represent the single object result
     */
    Class getCacheClass();

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
     * @return current status
     */
    TaskStatus getStatus();

    /**
     * Can this task will be merged with prev task with the same task key {@link ITask#getTaskKey()}
     *
     * @return is mergeable
     */
    boolean isMergeable();

    /**
     * True if task run as scheduled.
     *
     * @return true if scheduled, false otherwise
     */
    boolean isScheduled();

}
