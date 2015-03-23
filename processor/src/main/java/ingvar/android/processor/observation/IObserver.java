package ingvar.android.processor.observation;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IObserver<R> extends Comparable<R> {

    String KEY_GROUP = "observer.group";
    float MIN_PROGRESS = 0;
    float MAX_PROGRESS = 100;

    String getGroup();

    void completed(R result);

    void failed(Exception exception);

    void cancelled();

    void progress(float progress);

}
