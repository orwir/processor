package ingvar.android.processor.observation;

import java.util.Map;

import ingvar.android.processor.task.ITask;

/**
 * <br/><br/>Created by Igor Zubenko on 2015.03.18.
 */
public interface IObserverManager {

    /**
     * Bind task and observer
     *
     * @param task observable task
     * @param observer task observer
     */
    void add(ITask task, IObserver observer);

    /**
     * Unbind observer from task
     *
     * @param task observable task
     * @param observer task observer
     */
    void remove(ITask task, IObserver observer);

    /**
     * Unbind all observers from task
     *
     * @param task observable task
     */
    void remove(ITask task);

    /**
     * Unbind all observers by group from task
     *
     * @param group observers group
     */
    void removeGroup(String group);

    /**
     * Notifies observers of task about their progress.
     *
     * @param task observable task
     * @param progress progress
     * @param extra additional data
     */
    void notifyProgress(ITask task, float progress, Map extra);

    /**
     * Notifies observers of task about completion.
     *
     * @param task observable task
     * @param result task result
     * @param <R> result class
     */
    <R> void notifyCompleted(ITask task, R result);

    /**
     * Notifies observers of task about cancellation.
     *
     * @param task observable task
     */
    void notifyCancelled(ITask task);

    /**
     * Notifies observers of task about fail.
     *
     * @param task observable task
     * @param e process exception
     */
    void notifyFailed(ITask task, Exception e);

}
