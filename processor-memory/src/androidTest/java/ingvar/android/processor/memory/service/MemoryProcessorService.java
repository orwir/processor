package ingvar.android.processor.memory.service;

import ingvar.android.processor.memory.persistence.MemoryRepository;
import ingvar.android.processor.memory.source.MemorySource;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.service.ProcessorService;
import ingvar.android.processor.source.ISourceManager;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class MemoryProcessorService extends ProcessorService {

    @Override
    protected void provideSources(ISourceManager sourceManager) {
        sourceManager.addSource(MemorySource.class, new MemorySource());
    }

    @Override
    protected void providePersisters(ICacheManager cacheManager) {
        cacheManager.addPersister(new MemoryRepository());
    }

}
