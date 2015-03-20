package ingvar.android.processor.memory.persistence;

import android.util.LruCache;

import ingvar.android.processor.persistence.IRepository;
import ingvar.android.processor.persistence.Time;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class MemoryRepository<K, R> implements IRepository<K, R> {

    public static class Entry<V> {
        private long creationTime;
        private V value;

        public Entry(V value, long creationTime) {
            this.value = value;
            this.creationTime = creationTime;
        }

        public Entry(V value) {
            this(value, System.currentTimeMillis());
        }

        public long getCreationTime() {
            return creationTime;
        }

        public V getValue() {
            return value;
        }
    }

    private LruCache<K, Entry<R>> storage;
    private IRepository<K, R> decorated;

    public MemoryRepository(int maxSize) {
        this(maxSize, null);
    }

    public MemoryRepository(LruCache<K, Entry<R>> lruCache) {
        this(lruCache, null);
    }

    public MemoryRepository(int maxSize, IRepository<K, R> decorated) {
        storage = new LruCache<>(maxSize);
        this.decorated = decorated;
    }

    public MemoryRepository(LruCache<K, Entry<R>> lruCache, IRepository<K, R> decorated) {
        this.storage = lruCache;
        this.decorated = decorated;
    }

    @Override
    public R persist(K key, R data) {
        synchronized (storage) {
            storage.put(key, new Entry(data));
            if(decorated != null) {
                decorated.persist(key, data);
            }
        }
        return data;
    }

    @Override
    public R obtain(K key, long expiryTime) {
        R result = null;
        Entry entry = storage.get(key);

        if(entry != null) {
            if(expiryTime == Time.ALWAYS_RETURNED || (System.currentTimeMillis() - expiryTime) <= entry.creationTime) {
                result = (R) entry.getValue();
            }
        } else if (decorated != null) {
            //try to load from decorated repository
            result = decorated.obtain(key, expiryTime);
            if(result != null) {
                storage.put(key, new Entry(result, decorated.getCreationTime(key)));
            }
        }

        return result;
    }

    @Override
    public long getCreationTime(K key) {
        long time = -1;
        Entry entry = storage.get(key);
        if(entry != null) {
            time = entry.getCreationTime();

        } else if(decorated != null) {
            time = decorated.getCreationTime(key);
        }

        return time;
    }

    @Override
    public void remove(K key) {
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

}
