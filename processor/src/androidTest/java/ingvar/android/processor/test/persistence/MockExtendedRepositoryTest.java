package ingvar.android.processor.test.persistence;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

import ingvar.android.processor.persistence.CompositeKey;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.test.mock.MockExtendedRepository;

/**
 * Created by Igor Zubenko on 2015.04.23.
 */
public class MockExtendedRepositoryTest extends TestCase {

    private MockExtendedRepository<Object> repo;

    public MockExtendedRepositoryTest() {
        repo = new MockExtendedRepository<>(3, Object.class);
    }

    public void testCacheSingle() {
        String key = "key";
        String value = "value";

        repo.persist(key, value);

        String actual = repo.obtain(key, Time.ALWAYS_RETURNED);
        assertEquals(value, actual);
    }

    public void testCacheSingleExpired() throws Exception {
        String key = "key";
        String value = "value";

        repo.persist(key, value);
        Thread.sleep(20);

        String actual = repo.obtain(key, 15);
        assertNull(actual);
    }

    public void testCacheCollection() {
        CompositeKey<String> key = new CompositeKey<>("parent", "child1", "child2", "child3");
        List<String> values = Arrays.asList("value1", "value2", "value3");

        repo.persist(key, values);

        List<String> actual = repo.obtain(key, Time.ALWAYS_RETURNED);
        assertEquals(values, actual);

        assertEquals(Arrays.asList("value1"), repo.obtain(new CompositeKey<>("parent", "child1"), Time.ALWAYS_RETURNED));
    }

    public void testCacheCollectionWithoutParent() {
        CompositeKey<String> key = new CompositeKey<>(null, "child1", "child2", "child3");
        List<String> values = Arrays.asList("value1", "value2", "value3");

        repo.persist(key, values);

        List<String> actual = repo.obtain(key, Time.ALWAYS_RETURNED);
        assertEquals(values, actual);

        assertEquals("value1", repo.obtain("child1", Time.ALWAYS_RETURNED));
    }

    public void testCacheCollectionExpired() throws Exception {
        CompositeKey key = new CompositeKey<>("parent", "child1", "child2", "child3");
        List<String> values = Arrays.asList("value1", "value2", "value3");

        repo.persist(key, values);
        Thread.sleep(20);

        List<String> actual = repo.obtain(key, 10);
        assertNull(actual);
    }

    public void testRemoveSingle() {
        String key = "key";
        String value = "value";

        repo.persist(key, value);

        String actual = repo.obtain(key, Time.ALWAYS_RETURNED);
        assertEquals(value, actual);

        repo.remove(key);
        assertNull(repo.obtain(key, Time.ALWAYS_RETURNED));
    }

    public void testRemoveCollection() {
        CompositeKey key = new CompositeKey("parent", "child1", "child2", "child3");
        List<String> values = Arrays.asList("value1", "value2", "value3");

        repo.persist(key, values);

        List<String> actual = repo.obtain(key, Time.ALWAYS_RETURNED);
        assertEquals(values, actual);

        repo.remove(key);

        assertNull(repo.obtain(key, Time.ALWAYS_RETURNED));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(repo != null) {
            repo.removeAll();
        }
    }
}
