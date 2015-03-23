package ingvar.android.processor.observation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.request.IRequest;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class ObserverManager implements IObserverManager {

    protected static final String EXTRA_REQUEST = "request";
    protected static final String EXTRA_DATA = "data";
    protected static final String EXTRA_IS_PARCELABLE = "is_parcelable";

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
        Bundle bundle = new Bundle(3);
        bundle.putSerializable(EXTRA_REQUEST, request);
        if(data instanceof Serializable) {
            bundle.putSerializable(EXTRA_DATA, (Serializable) data);
        }
        else if(data instanceof Parcelable) {
            bundle.putParcelable(EXTRA_DATA, (Parcelable) data);
            bundle.putBoolean(EXTRA_IS_PARCELABLE, true);
        }
        else if(data != null) {
            throw new ProcessorException("Data must be implemented Serializable or Parcelable interface");
        }

        Message message = handler.obtainMessage(type.ordinal());
        message.setData(bundle);
        message.sendToTarget();
    }

    private enum Type {
        PROGRESS,
        COMPLETED,
        CANCELLED,
        FAILED;
    }

    private class ObserverCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            IRequest request = (IRequest) bundle.getSerializable(EXTRA_REQUEST);
            Type type = Type.values()[msg.what];
            Object data = null;
            if(bundle.containsKey(EXTRA_DATA)) {
                if(bundle.getBoolean(EXTRA_IS_PARCELABLE, false)) {
                    data = bundle.getParcelable(EXTRA_DATA);
                } else {
                    data = bundle.getSerializable(EXTRA_DATA);
                }
            }

            Set<IObserver> requestObservers = observers.get(request);
            if(requestObservers != null) {
                for(IObserver observer : requestObservers) {
                    switch (type) {
                        case PROGRESS:
                            observer.progress((Float) data);
                            break;
                        case COMPLETED:
                            observer.completed(data);
                            break;
                        case CANCELLED:
                            observer.cancelled();
                            break;
                        case FAILED:
                            observer.failed((Exception) data);
                            break;
                    }
                }
                if(!Type.PROGRESS.equals(type)) {
                    remove(request);
                }
            }
            bundle.clear();

            return true;
        }

    }

}
