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
import ingvar.android.processor.persistence.ListKey;
import ingvar.android.processor.persistence.Time;

import static ingvar.android.processor.util.BytesUtils.toBytes;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class FilesystemRepository extends AbstractRepository {

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
    public <K> long getCreationTime(K key) {
        String filename = filenameFromKey(key);
        return storage.getTime(filename);
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

    @Override
    protected Object composeKey(Object major, Object minor) {
        return filenameFromKey(major) + filenameFromKey(minor);
    }

    @Override
    protected <K, R> R persistSingle(K key, R data) {
        String filename = filenameFromKey(key);
        writeFile(filename, data);
        return data;
    }

    @Override
    protected <R> R persistCollection(ListKey key, R data) {
        Collection collection = (Collection) data;

        if(key.getMinors().size() != collection.size()) {
            throw new PersistenceException("Count of keys and data are mismatched!");
        }
        Iterator ikeys = key.getMinors().iterator();
        Iterator idata = collection.iterator();
        while(ikeys.hasNext()) {
            String compositeKey = (String) composeKey(key.getMajor(), ikeys.next());
            writeFile(compositeKey, idata.next());
        }
        return data;
    }

    @Override
    protected <K, R> R obtainSingle(K key, long expiryTime) {
        R result = null;
        String filename = filenameFromKey(key);
        if(storage.contains(filename) && fileNotExpired(filename, expiryTime)) {
            result = readFile(filename);
        }
        return result;
    }

    @Override
    protected List obtainCollection(ListKey key, long expiryTime) {
        List result = new ArrayList();

        List<String> filenameFilter = new ArrayList<>();
        for(Object minor : key.getMinors()) {
            filenameFilter.add((String) composeKey(key.getMajor(), minor));
        }
        String filenamePrefix = filenameFromKey(key.getMajor());
        boolean onlyPrefix = filenameFilter.isEmpty();

        for(String filename : storage.getAllKeys()) {
            if(filename.startsWith(filenamePrefix) && (onlyPrefix || filenameFilter.contains(filename))) {
                if(fileNotExpired(filename, expiryTime)) {
                    result.add(readFile(filename));
                }
            }
        }

        return result;
    }

    @Override
    protected <K> void removeSingle(K key) {
        String filename = filenameFromKey(key);
        storage.remove(filename);
    }

    @Override
    protected void removeList(ListKey key) {
        List<String> filenameFilter = new ArrayList<>();
        for(Object minor : key.getMinors()) {
            filenameFilter.add((String) composeKey(key.getMajor(), minor));
        }
        String filenamePrefix = filenameFromKey(key.getMajor());
        boolean onlyPrefix = filenameFilter.isEmpty();

        for(String filename : storage.getAllKeys()) {
            if(filename.startsWith(filenamePrefix) && (onlyPrefix || filenameFilter.contains(filename))) {
                storage.remove(filename);
            }
        }
    }

    protected <R> R readFile(String filename) {
        return storage.get(filename);
    }

    protected void writeFile(String filename, Object data) {
        storage.put(filename, (Serializable) data);
    }

    protected <K> String filenameFromKey(K key) {
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

    protected boolean fileNotExpired(String filename, long expiryTime) {
        long creationTime = storage.getTime(filename);
        return creationTime >= 0
                && (expiryTime == Time.ALWAYS_RETURNED
                || System.currentTimeMillis() - expiryTime <= creationTime);
    }

}
