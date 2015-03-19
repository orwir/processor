package ingvar.android.processor.ram.persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ingvar.android.processor.persistence.IPersister;
import ingvar.android.processor.persistence.Time;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class RamPersister implements IPersister<String, Object> {

    private final Map<String, Object> storage;
    private final Map<Object, Long> creationDate;

    public RamPersister() {
        storage = new ConcurrentHashMap<>();
        creationDate = new ConcurrentHashMap<>();
    }

    @Override
    public Object persist(String key, Object data) {
        storage.put(key, data);
        creationDate.put(data, System.currentTimeMillis());
        return data;
    }

    @Override
    public Object obtain(String key, long expiryTime) {
        Object result = storage.get(key);
        if(result != null) {
            Long ct = creationDate.get(result);
            if(ct == null || (expiryTime != Time.ALWAYS_RETURNED && (System.currentTimeMillis() - expiryTime) > ct.longValue())) {
                result = null;
            }
        }
        return result;
    }

    @Override
    public void remove(String key) {
        Object data = storage.remove(key);
        if(data != null) {
            creationDate.remove(data);
        }
    }

    @Override
    public void removeAll() {
        storage.clear();
        creationDate.clear();
    }

    @Override
    public Class<Object> getDataClass() {
        return Object.class;
    }

}
