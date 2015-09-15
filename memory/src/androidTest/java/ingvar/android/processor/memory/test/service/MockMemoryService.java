package ingvar.android.processor.memory.test.service;

import ingvar.android.processor.memory.persistence.BitmapMemoryRepository;
import ingvar.android.processor.memory.persistence.MemoryRepository;
import ingvar.android.processor.memory.source.MemorySource;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.service.ProcessorService;
import ingvar.android.processor.source.ContextSource;
import ingvar.android.processor.source.ISourceManager;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class MockMemoryService extends ProcessorService {

    @Override
    protected void provideSources(ISourceManager sourceManager) {
        sourceManager.setSource(MemorySource.class, new MemorySource());
        sourceManager.setSource(ContextSource.class, new ContextSource(this));
    }

    @Override
    protected void provideRepositories(ICacheManager cacheManager) {
        final int maxMemorySize = 5 * 1024 * 1024;

        cacheManager.addRepository(new BitmapMemoryRepository(maxMemorySize));
        cacheManager.addRepository(new MemoryRepository(maxMemorySize));
    }

}
