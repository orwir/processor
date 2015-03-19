package ingvar.android.processor.ram.request;

import java.util.concurrent.TimeUnit;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.ram.source.RamSource;
import ingvar.android.processor.request.SingleRequest;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class RamRequest extends SingleRequest<String, Object, RamSource> {

    public RamRequest(String key) {
        super(key, Object.class, RamSource.class, TimeUnit.HOURS.toMillis(1));
    }

    @Override
    public Object loadFromExternalSource(IObserverManager observerManager, RamSource source) {
        return source.get(getRequestKey());
    }

}
