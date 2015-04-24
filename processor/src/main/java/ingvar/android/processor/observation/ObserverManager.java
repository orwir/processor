package ingvar.android.processor.observation;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import ingvar.android.processor.task.ITask;

/**
 * Default implementation of observer manager.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.18.
 */
public class ObserverManager implements IObserverManager {

    protected final Map<ITask, Collection<IObserver>> observers;
    protected final Handler handler;

    public ObserverManager() {
        this.observers = new ConcurrentHashMap<>();
        handler = new Handler(Looper.getMainLooper(), new ObserverCallback());
    }

    @Override
    public void add(ITask task, IObserver observer) {
        Collection<IObserver> requestObservers = observers.get(task);
        if(requestObservers == null) {
            synchronized (task) {
                requestObservers = observers.get(task);
                if(requestObservers == null) {
                    requestObservers = new CopyOnWriteArraySet<>();
                    observers.put(task, requestObservers);
                }
            }
        }
        requestObservers.add(observer);
    }

    @Override
    public void remove(ITask task, IObserver observer) {
        Collection<IObserver> requestObservers = observers.get(observer);
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

        for(Map.Entry<ITask, Collection<IObserver>> entry : observers.entrySet()) {
            Collection<IObserver> taskObservers = entry.getValue();
            for(IObserver observer : taskObservers) {
                if(group.equals(observer.getGroup())) {
                    taskObservers.remove(observer);
                }
            }
            if(taskObservers.size() == 0) {
                observers.remove(entry.getKey());
            }
        }
    }

    @Override
    public void notifyProgress(ITask task, float progress, Map extra) {
        Message message = handler.obtainMessage(Type.IN_PROGRESS.ordinal(),
                new Wrapper(task, progress, extra));

        message.sendToTarget();
    }

    @Override
    public <R> void notifyCompleted(ITask task, R result) {
        Message message = handler.obtainMessage(Type.COMPLETED.ordinal(), new Wrapper(task, result));
        message.sendToTarget();
    }

    @Override
    public void notifyCancelled(ITask task) {
        Message message = handler.obtainMessage(Type.CANCELLED.ordinal(), new Wrapper(task, null));
        message.sendToTarget();
    }

    @Override
    public void notifyFailed(ITask task, Exception exception) {
        Message message = handler.obtainMessage(Type.FAILED.ordinal(), new Wrapper(task, exception));
        message.sendToTarget();
    }

    /**
     * Inner info about task status
     */
    protected enum Type {
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        FAILED;
    }

    /**
     * Wrapper for sending data to Main Thread.
     */
    protected class Wrapper {
        private ITask task;
        private Object data;
        private Map extra;

        public Wrapper(ITask task, Object data) {
            this(task, data, null);
        }

        public Wrapper(ITask task, Object data, Map extra) {
            this.task = task;
            this.data = data;
            this.extra = extra;
        }
    }

    /**
     * Callback for sending notifications to Main Thread.
     */
    protected class ObserverCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            Type type = Type.values()[msg.what];
            Wrapper wrapper = (Wrapper) msg.obj;

            Collection<IObserver> taskObservers = observers.get(wrapper.task);
            if(taskObservers != null) {
                for(IObserver observer : taskObservers) {
                    switch (type) {
                        case IN_PROGRESS:
                            observer.progress((Float) wrapper.data, wrapper.extra);
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
