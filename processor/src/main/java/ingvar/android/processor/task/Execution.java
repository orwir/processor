package ingvar.android.processor.task;

import java.util.concurrent.Future;

/**
 * Created by Igor Zubenko on 2015.07.10.
 */
public class Execution {

    private TaskStatus status;
    private Future future;
    private boolean cancelled;

    Execution() {
        status = TaskStatus.PENDING;
        future = null;
        cancelled = false;
    }

    public TaskStatus getStatus() {
        return status;
    }

    @SuppressWarnings("unchecked")
    public <R> Future<R> getFuture() {
        return future;
    }

    public void cancel() {
        if(future != null) {
            future.cancel(false);
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    void setFuture(Future future) {
        this.future = future;
    }

    void setStatus(TaskStatus status) {
        this.status = status;
    }

}
