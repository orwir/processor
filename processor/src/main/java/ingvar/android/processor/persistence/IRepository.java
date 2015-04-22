package ingvar.android.processor.persistence;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public interface IRepository {

    <K, R> R persist(K key, R data);

    <K, R> R obtain(K key, long expiryTime);

    /**
     * Return creation time of single object in the repository
     * @param key object key
     * @return creation time
     */
    <K> long getCreationTime(K key);

    <K> void remove(K key);

    void removeAll();

    boolean canHandle(Class dataClass);

}
