package ingvar.android.processor.observation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import ingvar.android.processor.request.IRequest;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class ObserverManager implements IObserverManager {

    public Map<IRequest, Set<IObserver>> observers;

    public ObserverManager() {
        this.observers = new ConcurrentHashMap<>();
    }

    @Override
    public void add(IRequest request, IObserver observer) {
        Set<IObserver> requestObservers = observers.get(request);
        if(requestObservers == null) {
            synchronized (request) {
                requestObservers = observers.get(request);
                if(requestObservers == null) {
                    requestObservers = new ConcurrentSkipListSet<>();
                    observers.put(request, requestObservers);
                }
            }
        }
        requestObservers.add(observer);
    }

    @Override
    public void remove(IRequest request, IObserver observer) {
        Set<IObserver> requestObservers = observers.get(observer);
        if(requestObservers != null) {
            requestObservers.remove(observer);
        }
    }

    @Override
    public void remove(IRequest request) {
        observers.remove(request);
    }

    @Override
    public void removeGroup(String group) {
        if(group == null) {
            throw new IllegalArgumentException("Group can't be null!");
        }

        for(Map.Entry<IRequest, Set<IObserver>> entry : observers.entrySet()) {
            Set<IObserver> requestObservers = entry.getValue();
            for(IObserver observer : requestObservers) {
                if(group.equals(observer.getGroup())) {
                    requestObservers.remove(observer);
                }
            }
        }
    }

    @Override
    public void notifyProgress(IRequest request, float progress) {
        Set<IObserver> requestObservers = observers.get(request);
        if(requestObservers != null) {
            for (IObserver observer : requestObservers) {
                observer.progress(progress);
            }
        }
    }

    @Override
    public <R> void notifyCompleted(IRequest request, R result) {
        Set<IObserver> requestObservers = observers.get(request);
        if(requestObservers != null) {
            for (IObserver observer : requestObservers) {
                observer.completed(result);
            }
        }
    }

    @Override
    public void notifyCancelled(IRequest request) {
        Set<IObserver> requestObservers = observers.get(request);
        if(requestObservers != null) {
            for (IObserver observer : requestObservers) {
                observer.cancelled();
            }
        }
    }

    @Override
    public void notifyFailed(IRequest request, Exception exception) {
        Set<IObserver> requestObservers = observers.get(request);
        if(requestObservers != null) {
            for (IObserver observer : requestObservers) {
                observer.failed(exception);
            }
        }
    }

}
