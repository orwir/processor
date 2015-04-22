package ingvar.android.processor.filesystem.persistence;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ingvar.android.processor.exception.PersistenceException;
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
        if(key instanceof CollectionKey) {
            CollectionKey<K> colKey = (CollectionKey) key;
            Collection colData = (Collection) data;
            if(colKey.getKeys().size() != colData.size()) {
                throw new PersistenceException("Count of keys and data are mismatched!");
            }

            Iterator<K> ki = colKey.getKeys().iterator();
            Iterator<Serializable> di = colData.iterator();
            while(ki.hasNext()) {
                K k = ki.next();
                Serializable d = di.next();

                String filename = colKey.getPrefix() + filenameFromKey(k);
                storage.put(filename, d);
            }

        } else {
            String filename = filenameFromKey(key);
            storage.put(filename, (Serializable) data);
        }
        return data;
    }

    @Override
    public R obtain(K key, long expiryTime) {
        R result = null;

        if(key instanceof CollectionKey) {
            CollectionKey<K> colKey = (CollectionKey) key;
            boolean onlyPrefix = colKey.getKeys().isEmpty();
            List<String> nameFilter = null;
            if(!onlyPrefix) {
                nameFilter = new ArrayList<>();
                for(K k : colKey.getKeys()) {
                    nameFilter.add(colKey.getPrefix() + filenameFromKey(k));
                }
            }

            List data = new ArrayList();
            for(String filename : storage.getAllKeys()) {
                if(filename.startsWith(colKey.getPrefix()) && (onlyPrefix || nameFilter.contains(filename))) {
                    if(fileNotExpired(filename, expiryTime)) {
                        data.add(storage.get(filename));
                    }
                }
            }
            result = (R) data;

        } else {
            String filename = filenameFromKey(key);
            if(storage.contains(filename) && isNotExpired(key, expiryTime)) {
                result = storage.get(filename);
            }
        }
        return result;
    }

    @Override
    public long getCreationTime(K key) {
        //TODO: CollectionKey
        String filename = filenameFromKey(key);
        return storage.getTime(filename);
    }

    @Override
    public void remove(K key) {
        //TODO: CollectionKey
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
            try {
                byte[] bytes;
                if (key instanceof String) {
                    bytes = ((String) key).getBytes("UTF-8");
                } else if (key instanceof Serializable) {
                    bytes = toBytes(key);
                } else {
                    bytes = key.toString().getBytes("UTF-8");
                }
                md5.reset();
                md5.update(bytes);
                return new BigInteger(1, md5.digest()).toString();

            } catch (Exception e) {
                return String.valueOf(key.hashCode());
            }
        } else {
            return String.valueOf(key.hashCode());
        }
    }

    protected boolean isNotExpired(K key, long expiryTime) {
        String filename = filenameFromKey(key);
        return fileNotExpired(filename, expiryTime);
    }

    protected boolean fileNotExpired(String filename, long expiryTime) {
        long creationTime = storage.getTime(filename);
        return creationTime >= 0
                && (expiryTime == Time.ALWAYS_RETURNED
                || System.currentTimeMillis() - expiryTime <= creationTime);
    }

}
