package ingvar.android.processor.memory.source;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ingvar.android.processor.source.ISource;
import ingvar.android.processor.util.BytesUtils;

/**
 * Simple memory source
 *
 * Created by Igor Zubenko on 2015.03.23.
 */
public class MemorySource implements ISource {

    private Map<Object, Object> storage;

    public MemorySource() {
        storage = new ConcurrentHashMap<>();
    }

    public void put(Object key, Object value) {
        storage.put(key, value);
    }

    public <R> R get(Object key) {
        return (R) storage.get(key);
    }

    /**
     *
     * @return count of objects in the storage
     */
    public int getCount() {
        return storage.size();
    }

    /**
     *
     * @return size in bytes of all objects in the storage
     */
    public int getSize() {
        int size = 0;
        for(Object obj : storage.values()) {
            size += BytesUtils.toBytes(obj).length;
        }
        return size;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

}
