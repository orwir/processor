package ingvar.android.processor.observation;

import ingvar.android.processor.task.ITask;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IObserverManager {

    void add(ITask task, IObserver observer);

    void remove(ITask task, IObserver observer);

    void remove(ITask task);

    void removeGroup(String group);

    void notifyProgress(ITask task, float progress); //TODO: add extras

    <R> void notifyCompleted(ITask task, R result);

    void notifyCancelled(ITask task);

    void notifyFailed(ITask task, Exception e);

}
