package ingvar.android.processor.filesystem.test.service;

import java.io.File;

import ingvar.android.processor.filesystem.persistence.FilesystemRepository;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.service.ProcessorService;
import ingvar.android.processor.source.ISourceManager;

/**
 * Created by Igor Zubenko on 2015.04.21.
 */
public class MockFilesystemService extends ProcessorService {

    public FilesystemSource getFilesystemSource() {
        return (FilesystemSource) getSourceManager().getSource(FilesystemSource.class);
    }

    @Override
    protected void provideSources(ISourceManager sourceManager) {
        sourceManager.setSource(FilesystemSource.class, new FilesystemSource(this));
    }

    @Override
    protected void provideRepositories(ICacheManager cacheManager) {
        cacheManager.addRepository(new FilesystemRepository(new File(getCacheDir(), "mock-cache"), 1024 * 1024 * 10));
    }

}
