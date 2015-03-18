package ingvar.android.processor.observation;

import ingvar.android.processor.request.IRequest;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IObserverManager {

    void add(IRequest request, IObserver observer);

    void remove(IRequest request, IObserver observer);

    void remove(IRequest request);

    void notifyProgress(IRequest request, float progress);

    <R> void notifyCompleted(IRequest request, R result);

    void notifyCancelled(IRequest request);

    void notifyFailed(IRequest request, Exception e);

}
