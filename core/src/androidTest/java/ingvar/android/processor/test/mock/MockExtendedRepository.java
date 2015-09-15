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
    protected Collection<R> persistCollection(CompositeKey<String> key, Collection<R> data) {
        if(key.getMinors().isEmpty()) {
            throw new PersistenceException("Minor keys are required!");
        }
        if(key.getMinors().size() != data.size()) {
            throw new PersistenceException("Count of minor keys and data mismatch!");
        }

        Iterator<String> iKey = key.getMinors().iterator();
        Iterator<R> iData = data.iterator();
        while (iKey.hasNext()) {
            persistSingle(composeKey(key.getMajor(), iKey.next()), iData.next());
        }

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
    protected Collection<R> obtainCollection(CompositeKey<String> key, long expiryTime) {
        if(key.getMinors().isEmpty()) {
            throw new PersistenceException("Minor keys are required!");
        }

        Collection<R> result = new ArrayList<>();
        for(String minor : key.getMinors()) {
            R single = obtainSingle(composeKey(key.getMajor(), minor), expiryTime);
            if(single == null) { //data set invalid
                result.clear();
                break;
            }
            result.add(single);
        }

        return result.isEmpty() ? null : result;
    }

    @Override
    protected void removeSingle(String key) {
        storage.remove(key);
    }

    @Override
    protected void removeCollection(CompositeKey<String> key) {
        if(key.getMinors().isEmpty()) {
            throw new PersistenceException("Minor keys are required!");
        }
        for(String minor : key.getMinors()) {
            removeSingle(composeKey(key.getMajor(), minor));
        }
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
