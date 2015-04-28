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
import ingvar.android.processor.persistence.AbstractRepository;
import ingvar.android.processor.persistence.CompositeKey;

import static ingvar.android.processor.util.BytesUtils.toBytes;

/**
 * Filesystem repository for caching tasks results.
 * <br/><br/>Created by Igor Zubenko on 2015.03.20.
 *
 * @param <K> identifier class
 * @param <R> result class
 */
public class FilesystemRepository<K, R> extends AbstractRepository<K, R> {

    protected DiskLruCache storage;
    protected MessageDigest md5;

    /**
     *
     * @param directory cache directory
     * @param maxBytes max size of cache
     */
    public FilesystemRepository(File directory, int maxBytes) {
        storage = DiskLruCache.open(directory, maxBytes);
        storage.setCommitType(DiskLruCache.CommitType.BY_UPDATES, 10);
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {}
    }

    @Override
    protected String composeKey(Object major, Object minor) {
        return filenameFromKey(major) + filenameFromKey(minor);
    }

    @Override
    protected R persistSingle(K key, R data) {
        String filename = filenameFromKey(key);
        writeFile(filename, data);
        return data;
    }

    @Override
    protected Collection<R> persistCollection(CompositeKey<K> key, Collection<R> data) {
        if(key.getMinors().size() != data.size()) {
            throw new PersistenceException("Count of keys and data are mismatched!");
        }

        Iterator<K> iKey = key.getMinors().iterator();
        Iterator<R> iData = data.iterator();
        while(iKey.hasNext()) {
            String filename = composeKey(key.getMajor(), iKey.next());
            writeFile(filename, iData.next());
        }
        return data;
    }

    @Override
    protected R obtainSingle(K key, long expiryTime) {
        R result = null;
        String filename = filenameFromKey(key);
        if(storage.contains(filename) && isNotExpired(storage.getTime(filename), expiryTime)) {
            result = readFile(filename);
        }
        return result;
    }

    @Override
    protected Collection<R> obtainCollection(CompositeKey<K> key, long expiryTime) {
        Collection<R> result = new ArrayList<>();

        List<String> filenameFilter = new ArrayList<>();
        for(K minor : key.getMinors()) {
            filenameFilter.add(composeKey(key.getMajor(), minor));
        }
        String filenamePrefix = filenameFromKey(key.getMajor());
        boolean onlyPrefix = filenameFilter.isEmpty();

        for(String filename : storage.getAllKeys()) {
            if(filename.startsWith(filenamePrefix) && (onlyPrefix || filenameFilter.contains(filename))) {
                if(isNotExpired(storage.getTime(filename), expiryTime)) {
                    result.add(readFile(filename));
                }
            }
        }
        if(!key.getMinors().isEmpty() && key.getMinors().size() != result.size()) {
            //If at least one item not found don't return anything
            result.clear();
        }
        return result.isEmpty() ? null : result;
    }

    @Override
    protected void removeSingle(K key) {
        String filename = filenameFromKey(key);
        storage.remove(filename);
    }

    @Override
    protected void removeCollection(CompositeKey<K> key) {
        List<String> filenameFilter = new ArrayList<>();
        for(K minor : key.getMinors()) {
            filenameFilter.add(composeKey(key.getMajor(), minor));
        }
        String filenamePrefix = filenameFromKey(key.getMajor());
        boolean onlyPrefix = filenameFilter.isEmpty();

        for(String filename : storage.getAllKeys()) {
            if(filename.startsWith(filenamePrefix) && (onlyPrefix || filenameFilter.contains(filename))) {
                storage.remove(filename);
            }
        }
    }

    @Override
    public void removeAll() {
        storage.removeAll();
    }

    @Override
    public long getCreationTime(Object key) {
        String filename = filenameFromKey(key);
        return storage.getTime(filename);
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

    /**
     * Read object from file.
     *
     * @param filename cache file identifier
     * @return object
     */
    protected R readFile(String filename) {
        return storage.get(filename);
    }

    /**
     * Write object to file.
     *
     * @param filename cache file identifier
     * @param data object
     */
    protected void writeFile(String filename, R data) {
        storage.put(filename, (Serializable) data);
    }

    /**
     * Create valid filename from identifier.
     *
     * @param key identifier
     * @return valid filename
     */
    protected String filenameFromKey(Object key) {
        //This method used for creating name from composite key
        //that's why handled null and empty.
        if(key == null) {
            return "";
        }
        else if(key instanceof String && ((String) key).isEmpty()) {
            return "";
        }
        else if(md5 != null) {
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

}
