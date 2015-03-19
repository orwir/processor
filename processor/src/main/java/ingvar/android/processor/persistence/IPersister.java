package ingvar.android.processor.persistence;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public interface IPersister<K, R> {

    R persist(K key, R data);

    R obtain(K key, long expiryTime);

    void remove(K key);

    void removeAll();

    Class<R> getDataClass();

}
