package ingvar.android.processor.ultimate.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ingvar.android.processor.filesystem.persistence.BitmapFilesystemRepository;
import ingvar.android.processor.filesystem.persistence.FilesystemRepository;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.filesystem.util.FileUtils;
import ingvar.android.processor.memory.persistence.BitmapMemoryRepository;
import ingvar.android.processor.memory.persistence.MemoryRepository;
import ingvar.android.processor.memory.source.MemorySource;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.service.ProcessorService;
import ingvar.android.processor.source.ContextSource;
import ingvar.android.processor.source.ISourceManager;
import ingvar.android.processor.util.LW;

/**
 * Created by Igor Zubenko on 2015.04.28.
 */
public class BaseProcessorService extends ProcessorService {

    protected static final int DEFAULT_BITMAP_MEMORY_CACHE_SIZE     = 40 * 1024 * 1024;
    protected static final int DEFAULT_BITMAP_FILESYSTEM_CACHE_SIZE = 20 * 1024 * 1024;

    protected static final int DEFAULT_MEMORY_CACHE_SIZE            = 25 * 1024 * 1024;
    protected static final int DEFAULT_FILESYSTEM_CACHE_SIZE        = 10 * 1024 * 1024;

    protected static final int INNER_BITMAP_FILESYSTEM_CACHE_SIZE   = 5 * 1024 * 1024;
    protected static final int INNER_FILESYSTEM_CACHE_SIZE          = 2 * 1024 * 1024;

    private boolean isExternal;
    private List<File> cacheDirs;

    public BaseProcessorService() {
        cacheDirs = new ArrayList<>(5);
        isExternal = true;
    }

    @Override
    protected void provideSources(ISourceManager sourceManager) {
        sourceManager.addSource(ContextSource.class, new ContextSource(this));
        sourceManager.addSource(MemorySource.class, new MemorySource());
        sourceManager.addSource(FilesystemSource.class, new FilesystemSource(this));
    }

    @Override
    protected void provideRepositories(ICacheManager cacheManager) {
        //BITMAP CACHE
        File dirBitmap = createCacheDir("lru-bitmap");
        int dirBitmapSize = getBitmapFilesystemCacheSize();
        int memoryBitmapSize = getMaxMemoryCacheSize(getBitmapMemoryCacheSize());

        BitmapFilesystemRepository bitmapFsRepo = new BitmapFilesystemRepository(dirBitmap, dirBitmapSize);
        BitmapMemoryRepository bitmapMemoryRepo = new BitmapMemoryRepository(memoryBitmapSize, bitmapFsRepo);
        cacheManager.addRepository(bitmapMemoryRepo);

        //COMMON CACHE
        File dirCommon = createCacheDir("lru-common");
        int dirCommonSize = getFilesystemCacheSize();
        int memoryCommonSize = getMaxMemoryCacheSize(getMemoryCacheSize());

        FilesystemRepository fsRepo = new FilesystemRepository(dirCommon, dirCommonSize);
        MemoryRepository memoryRepo = new MemoryRepository(memoryCommonSize, fsRepo);
        cacheManager.addRepository(memoryRepo);
    }

    /**
     * Return cache dirs what used by repositories.
     *
     * @return cache dirs
     */
    public List<File> getCacheDirs() {
        return cacheDirs;
    }

    /**
     * Notify service about cache dir used by repository.
     * @param file cache dir
     */
    protected void addCacheDir(File file) {
        cacheDirs.add(file);
    }

    /**
     * Create cache dir.
     * Used external cache dir if available.
     *
     * @param dirname lru-cache dir name or null.
     * @return new cache dir from app cache or root cache dir if dirname is null.
     */
    protected File createCacheDir(String dirname) {
        File cache = null;
        if(FileUtils.isExternalStorageWritable()) {
            cache = getExternalCacheDir();
        }
        if(cache == null) {
            cache = getCacheDir();
            isExternal = false;
            LW.w(TAG, "For caching will be used internal storage!");
        }
        if(dirname != null && !dirname.isEmpty()) {
            cache = new File(cache, dirname);
            if(cache.mkdirs()) {
                LW.d(TAG, "Created cache dir %s", cache.getAbsolutePath());
            }
        }
        addCacheDir(cache);

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
        return isExternal ? DEFAULT_FILESYSTEM_CACHE_SIZE : INNER_FILESYSTEM_CACHE_SIZE;
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
        return isExternal ? DEFAULT_BITMAP_FILESYSTEM_CACHE_SIZE : INNER_BITMAP_FILESYSTEM_CACHE_SIZE;
    }

}
