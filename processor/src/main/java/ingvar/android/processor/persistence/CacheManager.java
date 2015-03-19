package ingvar.android.processor.persistence;

import java.util.LinkedHashSet;
import java.util.Set;

import ingvar.android.processor.exception.PersistenceException;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class CacheManager implements ICacheManager {

    private final Set<IRepository> persisters;

    public CacheManager() {
        persisters = new LinkedHashSet<>();
    }

    @Override
    public void addPersister(IRepository persister) {
        persisters.add(persister);
    }

    @Override
    public void removePersister(IRepository persister) {
        persisters.remove(persister);
    }

    @Override
    public <K, R> R obtain(K key, Class dataClass, long expiryTime) {
        IRepository<K, R> persister = getAppropriatePersister(dataClass);
        return persister.obtain(key, expiryTime);
    }

    @Override
    public <K, R> R put(K key, R data) {
        IRepository<K, R> persister = getAppropriatePersister(data.getClass());
        persister.persist(key, data);
        return data;
    }

    @Override
    public <K> void remove(Class dataClass, K key) {
        IRepository persister = getAppropriatePersister(dataClass);
        persister.remove(key);
    }

    @Override
    public void remove(Class dataClass) {
        IRepository persister = getAppropriatePersister(dataClass);
        persister.removeAll();
    }

    @Override
    public void remove() {
        synchronized (persisters) {
            for (IRepository persister : persisters) {
                persister.removeAll();
            }
        }
    }

    protected IRepository getAppropriatePersister(Class dataClass) {
        IRepository appropriate = null;
        for(IRepository persister : persisters) {
            if(persister.canHandle(dataClass)) {
                appropriate = persister;
                break;
            }
        }
        if(appropriate == null) {
            throw new PersistenceException("Can't find appropriate persister for class: " + dataClass.getName());
        }
        return appropriate;
    }

}
