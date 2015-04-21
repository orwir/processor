package ingvar.android.processor.memory.test;

import android.util.LruCache;

import junit.framework.TestCase;

import ingvar.android.processor.memory.persistence.MemoryRepository;
import ingvar.android.processor.persistence.Time;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class DecorationTest extends TestCase {

    private MemoryRepository<String, String> decorated;
    private MemoryRepository<String, String> repo;
    private LruCache<String, MemoryRepository.Entry<String>> decoratedLru;
    private LruCache<String, MemoryRepository.Entry<String>> repoLru;

    public DecorationTest() {
        decoratedLru = new LruCache<>(3);
        decorated = new MemoryRepository<>(decoratedLru);
        repoLru = new LruCache<>(1);
        repo = new MemoryRepository<>(repoLru, decorated);
    }

    public void testDecoration() {
        String key = "test";
        String value = "test_value";

        repo.persist(key, value);

        assertEquals(1, decoratedLru.size());
        assertEquals(1, repoLru.size());
        assertEquals(value, repo.obtain(key, Time.ALWAYS_RETURNED));
        assertEquals(value, decorated.obtain(key, Time.ALWAYS_RETURNED));
    }

    public void testFromDecorated() {
        repo.persist("test1", "value1");
        assertEquals(1, repoLru.size());
        assertEquals(1, decoratedLru.size());

        repo.persist("test2", "value2");
        assertEquals(1, repoLru.size());
        assertEquals("value2", repo.obtain("test2", Time.ALWAYS_RETURNED));
        assertEquals(2, decoratedLru.size());
        assertEquals("value1", repo.obtain("test1", Time.ALWAYS_RETURNED));
        assertEquals("value1", repoLru.get("test1").getValue());
    }

}
