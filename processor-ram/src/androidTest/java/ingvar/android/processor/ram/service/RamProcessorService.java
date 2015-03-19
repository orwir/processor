package ingvar.android.processor.ram.service;

import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.ram.persistence.RamPersister;
import ingvar.android.processor.ram.source.RamSource;
import ingvar.android.processor.service.ProcessorService;
import ingvar.android.processor.source.ISourceManager;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class RamProcessorService extends ProcessorService {

    @Override
    protected void provideSources(ISourceManager sourceManager) {
        sourceManager.addSource(RamSource.class, new RamSource());
    }

    @Override
    protected void providePersisters(ICacheManager cacheManager) {
        cacheManager.addPersister(new RamPersister());
    }

}
