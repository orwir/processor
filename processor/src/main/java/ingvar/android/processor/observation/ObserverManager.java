package ingvar.android.processor.observation;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import ingvar.android.processor.task.ITask;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class ObserverManager implements IObserverManager {

    private Handler handler;
    private Map<ITask, Set<IObserver>> observers;

    public ObserverManager() {
        this.observers = new ConcurrentHashMap<>();
        handler = new Handler(Looper.getMainLooper(), new ObserverCallback());
    }

    @Override
    public void add(ITask task, IObserver observer) {
        Set<IObserver> requestObservers = observers.get(task);
        if(requestObservers == null) {
            synchronized (task) {
                requestObservers = observers.get(task);
                if(requestObservers == null) {
                    requestObservers = new ConcurrentSkipListSet<>();
                    observers.put(task, requestObservers);
                }
            }
        }
        requestObservers.add(observer);
    }

    @Override
    public void remove(ITask task, IObserver observer) {
        Set<IObserver> requestObservers = observers.get(observer);
        if(requestObservers != null) {
            requestObservers.remove(observer);
        }
    }

    @Override
    public void remove(ITask task) {
        observers.remove(task);
    }

    @Override
    public void removeGroup(String group) {
        if(group == null || group.isEmpty()) {
            throw new IllegalArgumentException("Group can't be null!");
        }

        for(Map.Entry<ITask, Set<IObserver>> entry : observers.entrySet()) {
            Set<IObserver> taskObservers = entry.getValue();
            for(IObserver observer : taskObservers) {
                if(group.equals(observer.getGroup())) {
                    taskObservers.remove(observer);
                }
            }
        }
    }

    @Override
    public void notifyProgress(ITask task, float progress) {
        sendMessage(task, Type.IN_PROGRESS, progress);
    }

    @Override
    public <R> void notifyCompleted(ITask task, R result) {
        sendMessage(task, Type.COMPLETED, result);
    }

    @Override
    public void notifyCancelled(ITask task) {
        sendMessage(task, Type.CANCELLED, null);
    }

    @Override
    public void notifyFailed(ITask task, Exception exception) {
        sendMessage(task, Type.FAILED, exception);
    }

    private void sendMessage(ITask request, Type type, Object data) {
        Message message = handler.obtainMessage(type.ordinal(), new Wrapper(request, data));
        message.sendToTarget();
    }

    private enum Type {
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        FAILED;
    }

    private class Wrapper {
        private ITask task;
        private Object data;

        public Wrapper(ITask task, Object data) {
            this.task = task;
            this.data = data;
        }
    }

    private class ObserverCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            Type type = Type.values()[msg.what];
            Wrapper wrapper = (Wrapper) msg.obj;

            Set<IObserver> taskObservers = observers.get(wrapper.task);
            if(taskObservers != null) {
                for(IObserver observer : taskObservers) {
                    switch (type) {
                        case IN_PROGRESS:
                            observer.progress((Float) wrapper.data);
                            break;
                        case COMPLETED:
                            observer.completed(wrapper.data);
                            break;
                        case CANCELLED:
                            observer.cancelled();
                            break;
                        case FAILED:
                            observer.failed((Exception) wrapper.data);
                            break;
                    }
                }
                if(!Type.IN_PROGRESS.equals(type)) {
                    remove(wrapper.task);
                }
            }
            return true;
        }

    }

}
