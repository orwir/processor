package ingvar.android.processor.persistence;

/**
 * Created by Igor Zubenko on 2015.04.22.
 */
public abstract class AbstractRepository implements IRepository {

    @Override
    public <K, R> R persist(K key, R data) {
        return key instanceof CompositeKey
            ? persistCollection((CompositeKey) key, data)
            : persistSingle(key, data);
    }

    @Override
    public <K, R> R obtain(K key, long expiryTime) {
        return key instanceof CompositeKey
                ? this.<R>obtainCollection((CompositeKey) key, expiryTime)
                : this.<K, R>obtainSingle(key, expiryTime);
    }

    @Override
    public <K> void remove(K key) {
        if(key instanceof CompositeKey) {
            removeCollection((CompositeKey) key);
        } else {
            removeSingle(key);
        }
    }

    protected abstract Object composeKey(Object major, Object minor);

    protected abstract <K, R> R persistSingle(K key, R data);

    protected abstract <R> R persistCollection(CompositeKey key, R data);

    protected abstract <K, R> R obtainSingle(K key, long expiryTime);

    protected abstract <R> R obtainCollection(CompositeKey key, long expiryTime);

    protected abstract <K> void removeSingle(K key);

    protected abstract void removeCollection(CompositeKey key);

    protected <K> boolean isNotExpired(K key, long expiryTime) {
        long creationTime = getCreationTime(key);
        return creationTime >= 0
                && (expiryTime == Time.ALWAYS_RETURNED
                || System.currentTimeMillis() - expiryTime <= creationTime);
    }

}
