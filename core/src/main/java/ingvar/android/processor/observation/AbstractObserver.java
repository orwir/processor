package ingvar.android.processor.observation;

import java.util.Map;

/**
 * Adapter for simplification code concrete realization.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.23.
 */
public abstract class AbstractObserver<R> implements IObserver<R> {

    @Override
    public void failed(Exception exception) {}

    @Override
    public void cancelled() {}

    @Override
    public void progress(float progress, Map extra) {}

    @Override
    public int compareTo(R another) {
        return 0;
    }

}
