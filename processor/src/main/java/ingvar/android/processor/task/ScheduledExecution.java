package ingvar.android.processor.task;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by Igor Zubenko on 2015.07.11.
 */
public class ScheduledExecution extends Execution {

    private long initialDelay;
    private long delay;
    private boolean repeatable;

    @Override
    public <R> ScheduledFuture<R> getFuture() {
        return (ScheduledFuture<R>) super.getFuture();
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public long getDelay() {
        return delay;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    void setDelay(long delay) {
        this.delay = delay;
    }

    void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
}
