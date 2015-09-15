package ingvar.android.processor.memory.source;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ingvar.android.processor.source.ISource;
import ingvar.android.processor.util.BytesUtils;

/**
 * Simple memory source.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.23.
 */
public class MemorySource implements ISource {

    private Map<Object, Object> storage;

    public MemorySource() {
        storage = new ConcurrentHashMap<>();
    }

    /**
     * Put object to source.
     *
     * @param key object identifier
     * @param value object
     */
    public void put(Object key, Object value) {
        storage.put(key, value);
    }

    /**
     * Get object from source.
     *
     * @param key object identifier
     * @param <R> object class
     * @return object
     */
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
     * Works correctly only if all objects are serializable
     *
     * @return size in bytes of all objects in the storage
     */
    public int getSize() {
        int size = 0;
        for(Object obj : storage.values()) {
            if(obj instanceof Serializable) {
                size += BytesUtils.toBytes(obj).length;
            } else {
                //TODO: not found solution for non-serializable objects
            }
        }
        return size;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

}
