package ingvar.android.processor.test.mock;

import android.util.LruCache;

import ingvar.android.processor.persistence.AbstractRepository;

/**
 * Created by Igor Zubenko on 2015.04.23.
 */
public class MockExtendedRepository<R> extends AbstractRepository<String, R> {

    private Class<R> dataClass;
    private LruCache<String, Entry> storage;

    public MockExtendedRepository(int capacity, Class dataClass) {
        this.dataClass = dataClass;
        this.storage = new LruCache<>(capacity);
    }

    @Override
    protected String composeKey(Object major, Object minor) {
        return (major == null ? "" : major.toString()) + minor.toString();
    }

    @Override
    protected R persistSingle(String key, R data) {
        storage.put(key, new Entry<>(data));
        return data;
    }

    @Override
    protected R obtainSingle(String key, long expiryTime) {
        Entry<R> data = storage.get(key);
        if(data != null && isNotExpired(data.creationTime, expiryTime)) {
            return data.value;
        }
        return null;
    }

    @Override
    protected void removeSingle(String key) {
        storage.remove(key);
    }

    @Override
    public void removeAll() {
        storage.evictAll();
    }

    @Override
    public long getCreationTime(Object key) {
        return storage.get((String) key).creationTime;
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return true;
    }

    private class Entry<V> {
        private V value;
        private long creationTime;

        public Entry(V value, long creationTime) {
            this.value = value;
            this.creationTime = creationTime;
        }

        public Entry(V value) {
            this(value, System.currentTimeMillis());
        }
    }

}
