package ingvar.android.processor.examples.service;

import java.io.File;

import ingvar.android.processor.examples.dictionary.persistence.MeaningConverter;
import ingvar.android.processor.examples.dictionary.persistence.WordConverter;
import ingvar.android.processor.examples.dictionary.pojo.Meaning;
import ingvar.android.processor.examples.dictionary.pojo.Word;
import ingvar.android.processor.examples.weather.network.RetrofitSource;
import ingvar.android.processor.examples.weather.persistence.WeatherContract;
import ingvar.android.processor.examples.weather.pojo.Weather;
import ingvar.android.processor.filesystem.persistence.BitmapFilesystemRepository;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.filesystem.util.FileUtils;
import ingvar.android.processor.memory.persistence.BitmapMemoryRepository;
import ingvar.android.processor.memory.persistence.MemoryRepository;
import ingvar.android.processor.memory.source.MemorySource;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.service.ProcessorService;
import ingvar.android.processor.source.ContextSource;
import ingvar.android.processor.source.ISourceManager;
import ingvar.android.processor.sqlite.persistence.SqliteRepository;
import ingvar.android.processor.sqlite.source.SqliteSource;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
public class ExampleService extends ProcessorService {

    @Override
    protected void provideSources(ISourceManager sourceManager) {
        sourceManager.setSource(RetrofitSource.class, new RetrofitSource(this));
        sourceManager.setSource(FilesystemSource.class, new FilesystemSource(this));
        sourceManager.setSource(MemorySource.class, new MemorySource());

        SqliteSource sqliteSource = new SqliteSource(this);
        sqliteSource.addConverter(Word.class, new WordConverter());
        sqliteSource.addConverter(Meaning.class, new MeaningConverter());
        sourceManager.setSource(SqliteSource.class, sqliteSource);
        sourceManager.setSource(ContextSource.class, new ContextSource(this));
    }

    @Override
    protected void provideRepositories(ICacheManager cacheManager) {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        final int maxMemoryCacheSize = maxMemory / 8;

        final int diskCache = 10 * 1024 * 1024; //10Mb
        final int memoryCache = Math.min(3 * 1024 * 1024, maxMemoryCacheSize); //3Mb

        File cacheDir = getCacheDir();
        if(FileUtils.isExternalStorageWritable()) {
            cacheDir = getExternalCacheDir();
        }
        cacheDir = new File(cacheDir, "lru"); //use inner folder for evade possible conflicts.

        BitmapFilesystemRepository bitmapFsRepo = new BitmapFilesystemRepository(cacheDir, diskCache);
        BitmapMemoryRepository<String> bitmapMemoryRepo = new BitmapMemoryRepository<>(memoryCache, bitmapFsRepo);


        cacheManager.addRepository(new SqliteRepository<>(this, WeatherContract.Weather.CONTENT_URI, Weather.class));
        cacheManager.addRepository(bitmapMemoryRepo);
        cacheManager.addRepository(new MemoryRepository(15));
    }

}
