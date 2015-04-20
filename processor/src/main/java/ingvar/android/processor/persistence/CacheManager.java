package ingvar.android.processor.persistence;

import java.util.LinkedHashSet;
import java.util.Set;

import ingvar.android.processor.exception.PersistenceException;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class CacheManager implements ICacheManager {

    private final Set<IRepository> repositories;

    public CacheManager() {
        repositories = new LinkedHashSet<>();
    }

    @Override
    public void addRepository(IRepository repository) {
        repositories.add(repository);
    }

    @Override
    public void removeRepository(IRepository persister) {
        repositories.remove(persister);
    }

    @Override
    public <K, R> R obtain(K key, Class dataClass, long expiryTime) {
        IRepository<K, R> repository = getAppropriateRepository(dataClass);
        return repository.obtain(key, expiryTime);
    }

    @Override
    public <K, R> R persist(K key, R data) {
        IRepository<K, R> repository = getAppropriateRepository(data.getClass());
        repository.persist(key, data);
        return data;
    }

    @Override
    public <K> void remove(Class dataClass, K key) {
        IRepository repository = getAppropriateRepository(dataClass);
        repository.remove(key);
    }

    @Override
    public void remove(Class dataClass) {
        IRepository repository = getAppropriateRepository(dataClass);
        repository.removeAll();
    }

    @Override
    public void remove() {
        synchronized (repositories) {
            for (IRepository repository : repositories) {
                repository.removeAll();
            }
        }
    }

    protected IRepository getAppropriateRepository(Class dataClass) {
        IRepository appropriate = null;
        for(IRepository repository : repositories) {
            if(repository.canHandle(dataClass)) {
                appropriate = repository;
                break;
            }
        }
        if(appropriate == null) {
            throw new PersistenceException("Can't find appropriate repository for class: " + dataClass.getName());
        }
        return appropriate;
    }

}
