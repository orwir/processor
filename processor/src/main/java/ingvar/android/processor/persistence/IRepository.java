package ingvar.android.processor.persistence;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public interface IRepository {

    /**
     * Persist data to repository
     *
     * @param key data identifier
     * @param data stored data
     * @param <K> identifier class
     * @param <R> data class
     * @return same data. (May be modified after persist. For example added ID.)
     */
    <K, R> R persist(K key, R data);

    /**
     * Obtain data from repository
     *
     * @param key identifier
     * @param expiryTime how much time data consider valid in the repository.
     * @param <K> identifier class
     * @param <R> data class
     * @return requested data
     */
    <K, R> R obtain(K key, long expiryTime);

    /**
     * Remove data from repository
     *
     * @param key identifier
     * @param <K> identifier class
     */
    <K> void remove(K key);

    /**
     * Remove all data from repository
     *
     */
    void removeAll();

    /**
     * Return creation time of single object in the repository
     *
     * @param key object identifier
     * @return creation time
     */
    long getCreationTime(Object key);

    /**
     * Can repository works with passed class or not.
     *
     * @param dataClass data class
     * @return true if can works, false otherwise
     */
    boolean canHandle(Class dataClass);

}
