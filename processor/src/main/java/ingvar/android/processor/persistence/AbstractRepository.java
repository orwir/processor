package ingvar.android.processor.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ingvar.android.processor.exception.PersistenceException;

/**
 * Base class for repositories what can work with CompositeKey.
 * It allows to persist/obtain collections.
 *
 * @param <K> identifier class
 * @param <R> single item class
 *
 * Created by Igor Zubenko on 2015.04.22.
 */
public abstract class AbstractRepository<K, R> implements IRepository {

    /**
     * Persist data to repository.
     *
     * @param key data identifier
     * @param data stored data
     * @param <KEY> identifier class. Possible variants: < K > or CompositeKey.
     * @param <RESULT> data class. Possible variants: < R > or Collection< R >
     * @return same data
     */
    @Override
    @SuppressWarnings("unchecked")
    public <KEY, RESULT> RESULT persist(KEY key, RESULT data) {
        return (key instanceof CompositeKey)
            ? (RESULT) persistCollection((CompositeKey) key, (Collection<R>) data)
            : (RESULT) persistSingle((K) key, (R) data);
    }

    /**
     * Obtain data from repository.
     *
     * @param key identifier
     * @param expiryTime how much time data consider valid in the repository.
     * @param <KEY> identifier class. Possible variants: < K > or CompositeKey.
     * @param <RESULT> data class. Possible variants: < R > or Collection< R >
     * @return requested data
     */
    @Override
    @SuppressWarnings("unchecked")
    public <KEY, RESULT> RESULT obtain(KEY key, long expiryTime) {
        return (key instanceof CompositeKey)
            ? (RESULT) obtainCollection((CompositeKey) key, expiryTime)
            : (RESULT) obtainSingle((K) key, expiryTime);
    }

    /**
     * Remove data from repository.
     *
     * @param key identifier
     * @param <KEY> identifier class. Possible variants: < K > or CompositeKey.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <KEY> void remove(KEY key) {
        if(key instanceof CompositeKey) {
            removeCollection((CompositeKey) key);
        } else {
            removeSingle((K) key);
        }
    }

    /**
     * Create identifier from major and minor keys.
     *
     * @param major parent key
     * @param minor child key
     * @return complex key
     */
    protected abstract K composeKey(Object major, Object minor);

    /**
     * Persist single item to repository.
     *
     * @param key identifier
     * @param data item
     * @return same item
     */
    protected abstract R persistSingle(K key, R data);

    /**
     * Obtain single item from repository.
     *
     * @param key identifier
     * @param expiryTime how much time data consider valid in the repository
     * @return requested data
     */
    protected abstract R obtainSingle(K key, long expiryTime);

    /**
     * Remove data from repository by single identifier
     *
     * @param key identifier
     */
    protected abstract void removeSingle(K key);

    /**
     * Persist collection of items.
     * Each items saved with (major + minor) key with same position as item.
     *
     * @param key identifier
     * @param data collection of items
     * @return same collection
     * @throws PersistenceException if CompositeKey does not contain minor keys or items and minor keys have different count.
     */
    protected Collection<R> persistCollection(CompositeKey<K> key, Collection<R> data) {
        if(key.getMinors().isEmpty()) {
            throw new PersistenceException("Minor keys are required!");
        }
        if(key.getMinors().size() != data.size()) {
            throw new PersistenceException("Count of minor keys and data mismatch!");
        }

        Iterator<K> iKey = key.getMinors().iterator();
        Iterator<R> iData = data.iterator();
        while (iKey.hasNext()) {
            persistSingle(composeKey(key.getMajor(), iKey.next()), iData.next());
        }

        return data;
    }

    /**
     * Obtain collection of items from repository.
     *
     * @param key identifier
     * @param expiryTime how much time data consider valid in the repository
     * @return requested collection or null
     * @throws PersistenceException if CompositeKey does not contain minor keys
     */
    protected Collection<R> obtainCollection(CompositeKey<K> key, long expiryTime) {
        if(key.getMinors().isEmpty()) {
            throw new PersistenceException("Minor keys are required!");
        }

        Collection<R> result = new ArrayList<>();
        for(K minor : key.getMinors()) {
            R single = obtainSingle(composeKey(key.getMajor(), minor), expiryTime);
            if(single == null) { //data set invalid
                result.clear();
                break;
            }
            result.add(single);
        }

        return result.isEmpty() ? null : result;
    }

    /**
     * Remove data from repository by CompositeKey.
     *
     * @param key identifier
     * @throws PersistenceException if CompositeKey does not contain minor keys
     */
    protected void removeCollection(CompositeKey<K> key) {
        if(key.getMinors().isEmpty()) {
            throw new PersistenceException("Minor keys are required!");
        }
        for(K minor : key.getMinors()) {
            removeSingle(composeKey(key.getMajor(), minor));
        }
    }

    /**
     *
     * @param creationTime when data was persisted to repository
     * @param expiryTime how much time data consider valid in the repository
     * @return true if not expired, false otherwise
     */
    protected boolean isNotExpired(long creationTime, long expiryTime) {
        return creationTime >= 0
                && (expiryTime == Time.ALWAYS_RETURNED
                || System.currentTimeMillis() - expiryTime <= creationTime);
    }

}
