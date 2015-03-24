package ingvar.android.processor.observation;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import ingvar.android.processor.request.IRequest;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class ObserverManager implements IObserverManager {

    private Handler handler;
    private Map<IRequest, Set<IObserver>> observers;

    public ObserverManager() {
        this.observers = new ConcurrentHashMap<>();
        handler = new Handler(Looper.getMainLooper(), new ObserverCallback());
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
        if(group == null || group.isEmpty()) {
            throw new IllegalArgumentException("Group can't be null!");
        }

        for(Map.Entry<IRequest, Set<IObserver>> entry : observers.entrySet()) {
            Set<IObserver> requestObservers = entry.getValue();
            for(IObserver observer : requestObservers) {
                if(observer.getGroup() != null && group.equals(observer.getGroup())) {
                    requestObservers.remove(observer);
                }
            }
        }
    }

    @Override
    public void notifyProgress(IRequest request, float progress) {
        sendMessage(request, Type.PROGRESS, progress);
    }

    @Override
    public <R> void notifyCompleted(IRequest request, R result) {
        sendMessage(request, Type.COMPLETED, result);
    }

    @Override
    public void notifyCancelled(IRequest request) {
        sendMessage(request, Type.CANCELLED, null);
    }

    @Override
    public void notifyFailed(IRequest request, Exception exception) {
        sendMessage(request, Type.FAILED, exception);
    }

    private void sendMessage(IRequest request, Type type, Object data) {
        Message message = handler.obtainMessage(type.ordinal(), new Transfer(request, data));
        message.sendToTarget();
    }

    private enum Type {
        PROGRESS,
        COMPLETED,
        CANCELLED,
        FAILED;
    }

    private class Transfer {
        private IRequest request;
        private Object data;

        public Transfer(IRequest request, Object data) {
            this.request = request;
            this.data = data;
        }
    }

    private class ObserverCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            Type type = Type.values()[msg.what];
            Transfer transfer = (Transfer) msg.obj;

            Set<IObserver> requestObservers = observers.get(transfer.request);
            if(requestObservers != null) {
                for(IObserver observer : requestObservers) {
                    switch (type) {
                        case PROGRESS:
                            observer.progress((Float) transfer.data);
                            break;
                        case COMPLETED:
                            observer.completed(transfer.data);
                            break;
                        case CANCELLED:
                            observer.cancelled();
                            break;
                        case FAILED:
                            observer.failed((Exception) transfer.data);
                            break;
                    }
                }
                if(!Type.PROGRESS.equals(type)) {
                    remove(transfer.request);
                }
            }
            return true;
        }

    }

}
