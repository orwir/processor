package ingvar.android.processor.memory.persistence;

import android.util.LruCache;

import ingvar.android.processor.persistence.IRepository;
import ingvar.android.processor.persistence.Time;

/**
 * Memory LRU-cache.
 * Note: collections saved on one key as is.
 * But in the decorated storage it may be splitted by minor keys.
 *
 * Created by Igor Zubenko on 2015.03.19.
 */
public class MemoryRepository implements IRepository {

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

    private LruCache<Object, Entry> storage;
    private IRepository decorated;

    public MemoryRepository(int maxSize) {
        this(maxSize, null);
    }

    public MemoryRepository(LruCache<?, ?> lruCache) {
        this(lruCache, null);
    }

    public MemoryRepository(int maxSize, IRepository decorated) {
        storage = new LruCache<>(maxSize);
        this.decorated = decorated;
    }

    @SuppressWarnings("unchecked")
    public MemoryRepository(LruCache<?, ?> lruCache, IRepository decorated) {
        this.storage = (LruCache<Object, Entry>) lruCache;
        this.decorated = decorated;
    }

    @Override
    public <K, R> R persist(K key, R data) {
        storage.put(key, new Entry<>(data));
        if(decorated != null) {
            decorated.persist(key, data);
        }
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, R> R obtain(K key, long expiryTime) {
        R result = null;
        Entry<R> entry = storage.get(key);

        if(entry != null) {
            if(expiryTime == Time.ALWAYS_RETURNED || (System.currentTimeMillis() - expiryTime) <= entry.creationTime) {
                result = entry.getValue();
            }
        } else if (decorated != null) {
            //try to load from decorated repository
            result = decorated.obtain(key, expiryTime);
            if(result != null) {
                storage.put(key, new Entry<>(result, decorated.getCreationTime(key)));
            }
        }

        return result;
    }

    @Override
    public <K> long getCreationTime(K key) {
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
    public <K> void remove(K key) {
        storage.remove(key);
        if(decorated != null) {
            decorated.remove(key);
        }
    }

    @Override
    public synchronized void removeAll() {
        synchronized (this) {
            storage.evictAll();
            if(decorated != null) {
                decorated.removeAll();
            }
        }
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return true;
    }

}
