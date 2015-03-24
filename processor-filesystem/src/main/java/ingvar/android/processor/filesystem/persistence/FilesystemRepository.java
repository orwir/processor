package ingvar.android.processor.filesystem.persistence;

import java.io.File;
import java.io.Serializable;

import ingvar.android.processor.filesystem.util.DiskLruCache;
import ingvar.android.processor.persistence.IRepository;
import ingvar.android.processor.persistence.Time;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class FilesystemRepository<T> implements IRepository<String, T> {
    //TODO: normalize keys
    protected DiskLruCache storage;

    public FilesystemRepository(File directory, int maxBytes) {
        storage = DiskLruCache.open(directory, maxBytes);
        storage.setCommitType(DiskLruCache.CommitType.BY_UPDATES, 10);
    }

    @Override
    public T persist(String key, T data) {
        storage.put(key, (Serializable) data);
        return data;
    }

    @Override
    public T obtain(String key, long expiryTime) {
        T result = null;
        if(storage.contains(key) && isNotExpired(key, expiryTime)) {
            result = storage.get(key);
        }
        return result;
    }

    @Override
    public long getCreationTime(String key) {
        return storage.getTime(key);
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

    protected boolean isNotExpired(String key, long expiryTime) {
        long creationTime = storage.getTime(key);
        return expiryTime == Time.ALWAYS_RETURNED || System.currentTimeMillis() - expiryTime <= creationTime;
    }

}
