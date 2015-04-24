package ingvar.android.processor.observation;

import java.util.Map;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IObserver<R> extends Comparable<R> {

    /**
     * Min progress value
     */
    float MIN_PROGRESS = 0;
    /**
     * Max progress value
     */
    float MAX_PROGRESS = 100;

    /**
     * Get observer group
     *
     * @return group
     */
    String getGroup();

    /**
     * Called when result ready.
     *
     * @param result result of process
     */
    void completed(R result);

    /**
     * Called when process fault.
     *
     * @param exception process exception
     */
    void failed(Exception exception);

    /**
     * Called when process was cancelled.
     */
    void cancelled();

    /**
     * Called when process notifies about their progress status.
     * @param progress progress
     * @param extra additional data
     */
    void progress(float progress, Map extra);

}
