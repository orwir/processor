package ingvar.android.processor.persistence;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface ICacheManager {

    <K, R> R obtain(K key, long expiryTime);

    <K, R> R put(K key, R data);

    <K> void remove(Class dataClass, K key);

    void remove(Class dataClass);

    void remove();

}
