package ingvar.android.processor.observation;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import ingvar.android.processor.task.ITask;
import ingvar.android.processor.task.TaskStatus;
import ingvar.android.processor.util.LW;

/**
 * Default implementation of observer manager.
 * Logged under VERBOSE level.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.18.
 */
public class ObserverManager implements IObserverManager {

    public static final String TAG = ObserverManager.class.getSimpleName();

    protected final Map<ITask, Collection<IObserver>> observers;
    protected final Handler handler;
    private final Object lock = new Object();

    public ObserverManager() {
        this.observers = new ConcurrentHashMap<>();
        handler = new Handler(Looper.getMainLooper(), new ObserverCallback());
    }

    @Override
    public void add(ITask task, IObserver observer) {
        Collection<IObserver> requestObservers = observers.get(task);
        if(requestObservers == null) {
            synchronized (lock) {
                requestObservers = observers.get(task);
                if(requestObservers == null) {
                    requestObservers = new CopyOnWriteArraySet<>();
                    observers.put(task, requestObservers);
                }
            }
        }
        requestObservers.add(observer);
        LW.v(TAG, "Added observer for task '%s'", task);
    }

    @Override
    public void remove(ITask task, IObserver observer) {
        Collection<IObserver> requestObservers = observers.get(task);
        if(requestObservers != null) {
            requestObservers.remove(observer);
            LW.v(TAG, "Removed observer from task %s", task);
        }
    }

    @Override
    public void remove(ITask task) {
        observers.remove(task);
        LW.v(TAG, "Removed all observers from task %s", task);
    }

    @Override
    public void removeGroup(String group) {
        if(group == null || group.isEmpty()) {
            throw new IllegalArgumentException("Group can't be null!");
        }

        synchronized (lock) {
            for (Map.Entry<ITask, Collection<IObserver>> entry : observers.entrySet()) {
                Collection<IObserver> taskObservers = entry.getValue();
                for (IObserver observer : taskObservers) {
                    if (group.equals(observer.getGroup())) {
                        taskObservers.remove(observer);
                    }
                }
                if (taskObservers.size() == 0) {
                    observers.remove(entry.getKey());
                }
            }
        }
        LW.v(TAG, "Removed all observers from all tasks by group '%s'", group);
    }

    @Override
    public void notifyProgress(ITask task, float progress, Map extra) {
        Message message = handler.obtainMessage(Status.IN_PROGRESS.ordinal(),
                new Wrapper(task, progress, extra));
        message.sendToTarget();
    }

    @Override
    public <R> void notifyCompleted(ITask task, R result) {
        Message message = handler.obtainMessage(Status.COMPLETED.ordinal(), new Wrapper(task, result));
        message.sendToTarget();
    }

    @Override
    public void notifyCancelled(ITask task) {
        Message message = handler.obtainMessage(Status.CANCELLED.ordinal(), new Wrapper(task, null));
        message.sendToTarget();
    }

    @Override
    public void notifyFailed(ITask task, Exception exception) {
        Message message = handler.obtainMessage(Status.FAILED.ordinal(), new Wrapper(task, exception));
        message.sendToTarget();
    }

    /**
     * Inner info about task status
     */
    protected enum Status {
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
        @SuppressWarnings("unchecked")
        public boolean handleMessage(Message msg) {
            final Status status = Status.values()[msg.what];
            final Wrapper wrapper = (Wrapper) msg.obj;
            final TaskStatus taskStatus = wrapper.task.getStatus();

            Collection<IObserver> taskObservers = observers.get(wrapper.task);
            if (taskObservers != null) {
                synchronized (lock) {
                    for (IObserver observer : taskObservers) {
                        switch (status) {
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
                    if (!Status.IN_PROGRESS.equals(status)) {
                        remove(wrapper.task);
                    }
                    LW.v(TAG, "Received message {'task': %s, 'task status': '%s', 'status': '%s'}", wrapper.task, taskStatus, status);
                }
            }
            return true;
        }

    }

}
