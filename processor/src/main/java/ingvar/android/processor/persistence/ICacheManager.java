package ingvar.android.processor.persistence;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface ICacheManager {

    void addRepository(IRepository persister);

    void removeRepository(IRepository persister);

    <K, R> R obtain(K key, Class dataClass, long expiryTime);

    <K, R> R persist(K key, R data);

    <K> void remove(Class dataClass, K key);

    void remove(Class dataClass);

    void remove();

}
