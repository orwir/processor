package ingvar.android.processor.filesystem.test;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.io.File;

import ingvar.android.processor.filesystem.util.DiskLruCache;

/**
 * Created by Igor Zubenko on 2015.04.21.
 */
public class DiskLruCacheTest extends ApplicationTestCase<Application> {

    private File directory;
    private DiskLruCache cache;

    public DiskLruCacheTest() {
        super(Application.class);
    }

    public void testGetFiles() {
        cache.put("key1", "data1");
        cache.put("key2", "data2");
        cache.put("key3", "data3");

        File[] files = cache.getAll();
        assertEquals("Cached files mismatch", 3, files.length);
    }

    public void testCache() {
        String data = "small_data";
        cache.put("key", data);

        File cached = cache.getFile("key");
        assertNotNull("Cached file not found", cached);
        assertEquals("Cached file size not right", data.length(), cached.length() - 7); //7 bytes String class overhead
        String cachedData = cache.get("key");
        assertEquals("Cached data not match", data, cachedData);
    }

    public void testCacheExtended() {
        String _500kb = generateFile(500);

        cache.put("key1", _500kb);
        cache.put("key2", _500kb);
        cache.put("key3", _500kb);

        //wait async adjusting
        try {Thread.sleep(20);} catch (InterruptedException e) {}

        File[] files = cache.getAll();
        assertEquals("Least file must be deleted", 2, files.length);

        File cached1 = cache.get("key1");
        assertNull("Least file must be deleted", cached1);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        directory = new File(getContext().getCacheDir(), "lru_cache");
        new File(directory, "journal.lock").delete();
        cache = DiskLruCache.open(directory, 1024); //1KB
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(cache != null) {
            cache.removeAll();
            cache.close();
        }
    }

    private String generateFile(int size) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < size; i++) {
            builder.append("A");
        }
        return builder.toString();
    }

}
