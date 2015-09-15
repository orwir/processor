package ingvar.android.processor.test.mock;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.service.ProcessorService;
import ingvar.android.processor.source.ISourceManager;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class MockService extends ProcessorService {

    public ExtendedObserverManager getObserverManager() {
        return (ExtendedObserverManager) super.getObserverManager();
    }

    @Override
    protected void provideSources(ISourceManager sourceManager) {
        sourceManager.addSource(MockSource.class, new MockSource());
    }

    @Override
    protected void provideRepositories(ICacheManager cacheManager) {
        cacheManager.addRepository(new MockRepository());
    }

    @Override
    protected IObserverManager createObserverManager() {
        return new ExtendedObserverManager();
    }

}
