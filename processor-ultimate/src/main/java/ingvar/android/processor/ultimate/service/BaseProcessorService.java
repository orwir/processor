package ingvar.android.processor.ultimate.service;

import java.io.File;

import ingvar.android.processor.filesystem.persistence.BitmapFilesystemRepository;
import ingvar.android.processor.filesystem.persistence.FilesystemRepository;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.filesystem.util.FileUtils;
import ingvar.android.processor.memory.persistence.BitmapMemoryRepository;
import ingvar.android.processor.memory.persistence.MemoryRepository;
import ingvar.android.processor.memory.source.MemorySource;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.service.ProcessorService;
import ingvar.android.processor.source.ISourceManager;

/**
 * Created by Igor Zubenko on 2015.04.28.
 */
public class BaseProcessorService extends ProcessorService {

    protected static final int DEFAULT_BITMAP_MEMORY_CACHE_SIZE     = 40 * 1024 * 1024;
    protected static final int DEFAULT_BITMAP_FILESYSTEM_CACHE_SIZE = 25 * 1024 * 1024;

    protected static final int DEFAULT_MEMORY_CACHE_SIZE            = 30 * 1024 * 1024;
    protected static final int DEFAULT_FILESYSTEM_CACHE_SIZE        = 20 * 1024 * 1024;

    @Override
    protected void provideSources(ISourceManager sourceManager) {
        sourceManager.addSource(MemorySource.class, new MemorySource());
        sourceManager.addSource(FilesystemSource.class, new FilesystemSource(this));
    }

    @Override
    protected void provideRepositories(ICacheManager cacheManager) {
        //BITMAP CACHE
        File dirBitmap = getCacheDir("lru-bitmap");
        int dirBitmapSize = getBitmapFilesystemCacheSize();
        int memoryBitmapSize = getMaxMemoryCacheSize(getBitmapMemoryCacheSize());

        BitmapFilesystemRepository bitmapFsRepo = new BitmapFilesystemRepository(dirBitmap, dirBitmapSize);
        BitmapMemoryRepository bitmapMemoryRepo = new BitmapMemoryRepository(memoryBitmapSize, bitmapFsRepo);
        cacheManager.addRepository(bitmapMemoryRepo);

        //COMMON CACHE
        File dirCommon = getCacheDir("lru-common");
        int dirCommonSize = getFilesystemCacheSize();
        int memoryCommonSize = getMaxMemoryCacheSize(getMemoryCacheSize());

        FilesystemRepository fsRepo = new FilesystemRepository(dirCommon, dirCommonSize);
        MemoryRepository memoryRepo = new MemoryRepository(memoryCommonSize, fsRepo);
        cacheManager.addRepository(memoryRepo);
    }

    /**
     * Get cache dir.
     * Used external cache dir if available.
     *
     * @param dirname lru-cache dir name or null.
     * @return new cache dir from app cache or root cache dir if dirname is null.
     */
    protected File getCacheDir(String dirname) {
        File cache;
        if(FileUtils.isExternalStorageWritable()) {
            cache = getExternalCacheDir();
        } else {
            cache = getCacheDir();
        }
        if(dirname != null && !dirname.isEmpty()) {
            cache = new File(cache, dirname);
            cache.mkdirs();
        }
        return cache;
    }

    /**
     * Get min value of 1/8 of memory or preferred size.
     *
     * @param preferredSize preferred size (in bytes)
     * @return allowable memory size in bytes.
     */
    protected int getMaxMemoryCacheSize(int preferredSize) {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        final int maxMemoryCacheSize = maxMemory / 8;
        return Math.min(maxMemoryCacheSize, preferredSize);
    }

    /**
     * Get memory cache size.
     *
     * @return memory cache size in bytes
     */
    protected int getMemoryCacheSize() {
        return DEFAULT_MEMORY_CACHE_SIZE;
    }

    /**
     * Get filesystem cache size.
     *
     * @return cache size in bytes.
     */
    protected int getFilesystemCacheSize() {
        return DEFAULT_FILESYSTEM_CACHE_SIZE;
    }

    /**
     * Get bitmap memory cache size.
     *
     * @return bitmap cache size in bytes
     */
    protected int getBitmapMemoryCacheSize() {
        return DEFAULT_BITMAP_MEMORY_CACHE_SIZE;
    }

    /**
     * Get bitmap filesystem cache size.
     *
     * @return bitmap cache size in bytes
     */
    protected int getBitmapFilesystemCacheSize() {
        return DEFAULT_BITMAP_FILESYSTEM_CACHE_SIZE;
    }

}
