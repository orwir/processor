package ingvar.android.processor.test.mock;

import java.util.Collection;
import java.util.Map;

import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.observation.ObserverManager;
import ingvar.android.processor.task.ITask;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class ExtendedObserverManager extends ObserverManager {

    public Map<ITask, Collection<IObserver>> getObservers() {
        return observers;
    }

    public Collection<IObserver> getObservers(ITask task) {
        return observers.get(task);
    }

}
