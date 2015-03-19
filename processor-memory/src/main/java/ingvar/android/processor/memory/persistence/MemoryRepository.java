package ingvar.android.processor.memory.persistence;

import android.util.LruCache;

import ingvar.android.processor.persistence.IRepository;
import ingvar.android.processor.persistence.Time;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class MemoryRepository implements IRepository<Object, Object> {

    private LruCache<Object, Entry> storage;

    public MemoryRepository() {
        storage = new LruCache<>(20);
    }

    @Override
    public Object persist(Object key, Object data) {
        synchronized (storage) {
            storage.put(key, new Entry(data));
        }
        return data;
    }

    @Override
    public Object obtain(Object key, long expiryTime) {
        Object result = null;
        Entry entry;
        synchronized (storage) {
            entry = storage.get(key);
        }
        if(entry != null) {
            if(expiryTime == Time.ALWAYS_RETURNED || (System.currentTimeMillis() - expiryTime) <= entry.creationTime) {
                result = entry.value;
            }
        }
        return result;
    }

    @Override
    public void remove(Object key) {
        synchronized (storage) {
            storage.remove(key);
        }
    }

    @Override
    public synchronized void removeAll() {
        synchronized (storage) {
            storage.evictAll();
        }
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return true;
    }

    private class Entry {
        long creationTime;
        Object value;

        public Entry(Object value, long creationTime) {
            this.value = value;
            this.creationTime = creationTime;
        }

        public Entry(Object value) {
            this(value, System.currentTimeMillis());
        }
    }

}
