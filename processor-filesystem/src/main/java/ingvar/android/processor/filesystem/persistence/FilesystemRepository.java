package ingvar.android.processor.filesystem.persistence;

import java.io.File;
import java.io.Serializable;

import ingvar.android.processor.persistence.IRepository;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class FilesystemRepository<T> implements IRepository<String, T> {

    protected DiskLruCache storage;

    public FilesystemRepository(File directory, int maxBytes) {
        storage = DiskLruCache.open(directory, maxBytes);
    }

    @Override
    public T persist(String key, T data) {
        storage.put(key, (Serializable) data);
        return data;
    }

    @Override
    public T obtain(String key, long expiryTime) {
        T result = null;
        if(storage.contains(key)) {
            long creationTime = storage.getTime(key);
            if((System.currentTimeMillis() - expiryTime) <= creationTime) {
                result = storage.get(key);
            }
        }
        return result;
    }

    @Override
    public void remove(String key) {
        storage.remove(key);
    }

    @Override
    public void removeAll() {
        storage.removeAll();
    }

    @Override
    public boolean canHandle(Class dataClass) {
        for(Class i : dataClass.getInterfaces()) {
            if(i.equals(Serializable.class)) {
                return true;
            }
        }
        return false;
    }

}
