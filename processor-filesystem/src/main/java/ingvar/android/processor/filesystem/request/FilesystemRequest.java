package ingvar.android.processor.filesystem.request;

import java.io.Serializable;

import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.request.SingleRequest;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class FilesystemRequest<T extends Serializable> extends SingleRequest<String, T, FilesystemSource> {

    public FilesystemRequest(String key, Class<T> resultClass, long cacheExpirationTime) {
        super(key, resultClass, FilesystemSource.class, cacheExpirationTime);
    }

    @Override
    public T loadFromExternalSource(IObserverManager observerManager, FilesystemSource source) {
        //TODO:
        return null;
    }

}
