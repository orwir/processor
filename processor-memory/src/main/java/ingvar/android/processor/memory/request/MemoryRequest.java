package ingvar.android.processor.memory.request;

import java.util.concurrent.TimeUnit;

import ingvar.android.processor.memory.source.MemorySource;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.request.SingleRequest;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class MemoryRequest extends SingleRequest<String, Object, MemorySource> {

    public MemoryRequest(String key) {
        super(key, Object.class, MemorySource.class, TimeUnit.HOURS.toMillis(1));
    }

    @Override
    public Object loadFromExternalSource(IObserverManager observerManager, MemorySource source) {
        return source.get(getRequestKey());
    }

}
