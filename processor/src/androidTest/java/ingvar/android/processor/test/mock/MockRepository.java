package ingvar.android.processor.test.mock;

import java.util.HashMap;
import java.util.Map;

import ingvar.android.processor.persistence.IRepository;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class MockRepository implements IRepository {

    private Map storage = new HashMap();

    @Override
    public Object persist(Object key, Object data) {
        return storage.put(key, data);
    }

    @Override
    public Object obtain(Object key, long expiryTime) {
        return storage.get(key);
    }

    @Override
    public long getCreationTime(Object key) {
        return System.currentTimeMillis();
    }

    @Override
    public void remove(Object key) {
        storage.remove(key);
    }

    @Override
    public void removeAll() {
        storage.clear();
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return true;
    }

}
