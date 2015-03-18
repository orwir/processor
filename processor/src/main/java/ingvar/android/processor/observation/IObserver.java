package ingvar.android.processor.observation;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IObserver {

    float MIN_PROGRESS = 0;
    float MAX_PROGRESS = 100;

    void progress(float progress);

    <R> void completed(R result);

    void cancelled();

    void failed(Exception exception);

}
