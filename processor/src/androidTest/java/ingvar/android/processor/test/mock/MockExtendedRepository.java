package ingvar.android.processor.test.mock;

import android.util.LruCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ingvar.android.processor.exception.PersistenceException;
import ingvar.android.processor.persistence.AbstractRepository;
import ingvar.android.processor.persistence.CompositeKey;

/**
 * Created by Igor Zubenko on 2015.04.23.
 */
public class MockExtendedRepository<R> extends AbstractRepository<String, R> {

    private Class<R> dataClass;
    private LruCache<Object, Entry> storage;

    public MockExtendedRepository(int capacity, Class dataClass) {
        this.dataClass = dataClass;
        this.storage = new LruCache(capacity);
    }

    @Override
    protected Object composeKey(Object major, Object minor) {
        return (major == null ? "" : major.toString()) + minor.toString();
    }

    @Override
    protected R persistSingle(String key, R data) {
        storage.put(key, new Entry<>(data));
        return data;
    }

    @Override
    protected Collection<R> persistCollection(CompositeKey key, Collection<R> data) {
        if(key.getMinors().size() > 0) {
            if(key.getMinors().size() != data.size()) {
                throw new PersistenceException("Count of minor keys and data mismatch!");
            }
            Iterator ik = key.getMinors().iterator();
            Iterator id = data.iterator();
            while (ik.hasNext()) {
                storage.put(composeKey(key.getMajor(), ik.next()), new Entry(id.next()));
            }
        } else {
            storage.put(key.getMajor(), new Entry<>(data));
        }
        return data;
    }

    @Override
    protected R obtainSingle(String key, long expiryTime) {
        Entry<R> entry = storage.get(key);
        if(entry != null && isNotExpired(entry.creationTime, expiryTime)) {
            return entry.value;
        }
        return null;
    }

    @Override
    protected Collection<R> obtainCollection(CompositeKey key, long expiryTime) {
        Collection<R> result = new ArrayList<>();
        if(key.getMinors().size() > 0) {
            for(Object minor : key.getMinors()) {
                R single = obtainSingle((String) composeKey(key.getMajor(), minor), expiryTime);
                if(single != null) {
                    result.add(single);
                } else {
                    result.clear();
                    break;
                }
            }
        } else {
            Entry<Collection<R>> entry = storage.get(key);
            if(entry != null && isNotExpired(entry.creationTime, expiryTime)) {
                result = entry.value;
            }
        }
        return result.isEmpty() ? null : result;
    }

    @Override
    protected void removeSingle(String key) {
        storage.remove(key);
    }

    @Override
    protected void removeCollection(CompositeKey key) {
        if(key.getMinors().isEmpty()) {
            removeSingle((String) key.getMajor());
        } else {
            for(Object minor : key.getMinors()) {
                removeSingle((String) composeKey(key.getMajor(), minor));
            }
        }
    }

    @Override
    public void removeAll() {

    }

    @Override
    public long getCreationTime(Object key) {
        return storage.get(key).creationTime;
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
