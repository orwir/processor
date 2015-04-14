package ingvar.android.processor.filesystem.persistence;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ingvar.android.processor.filesystem.util.DiskLruCache;
import ingvar.android.processor.persistence.IRepository;
import ingvar.android.processor.persistence.Time;

import static ingvar.android.processor.util.BytesUtils.toBytes;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class FilesystemRepository<K, R> implements IRepository<K, R> {

    protected DiskLruCache storage;
    protected MessageDigest md5;

    public FilesystemRepository(File directory, int maxBytes) {
        storage = DiskLruCache.open(directory, maxBytes);
        storage.setCommitType(DiskLruCache.CommitType.BY_UPDATES, 10);
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {}
    }

    @Override
    public R persist(K key, R data) {
        String filename = filenameFromKey(key);
        storage.put(filename, (Serializable) data);
        return data;
    }

    @Override
    public R obtain(K key, long expiryTime) {
        String filename = filenameFromKey(key);
        R result = null;
        if(storage.contains(filename) && isNotExpired(key, expiryTime)) {
            result = storage.get(filename);
        }
        return result;
    }

    @Override
    public long getCreationTime(K key) {
        String filename = filenameFromKey(key);
        return storage.getTime(filename);
    }

    @Override
    public void remove(K key) {
        String filename = filenameFromKey(key);
        storage.remove(filename);
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

    protected String filenameFromKey(K key) {
        if(md5 != null) {
            md5.reset();
            md5.update(toBytes(key));
            return new BigInteger(1, md5.digest()).toString();
        } else {
            return String.valueOf(key.hashCode());
        }
    }

    protected boolean isNotExpired(K key, long expiryTime) {
        String filename = filenameFromKey(key);
        long creationTime = storage.getTime(filename);
        return creationTime >= 0
            && (expiryTime == Time.ALWAYS_RETURNED
            || System.currentTimeMillis() - expiryTime <= creationTime);
    }

}
