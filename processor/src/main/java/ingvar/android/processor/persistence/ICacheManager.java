package ingvar.android.processor.persistence;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface ICacheManager {

    /**
     * Add repository to cache manager.
     *
     * @param repository repository
     */
    void addRepository(IRepository repository);

    /**
     * Remove repository from cache manager.
     *
     * @param repository
     */
    void removeRepository(IRepository repository);

    /**
     * Obtain data from appropriate repository.
     *
     * @param key data identifier
     * @param dataClass data class
     * @param expiryTime how much time data in the repository is valid.
     * @param <K> identifier class
     * @param <R> result class
     * @return data
     */
    <K, R> R obtain(K key, Class dataClass, long expiryTime);

    /**
     * Save data to appropriate repository.
     *
     * @param key data identifier
     * @param data data
     * @param <K> identifier class
     * @param <R> result class
     * @return same data (May be changed. For examples added repository IDs)
     */
    <K, R> R persist(K key, R data);

    /**
     * Remove data from repository.
     *
     * @param dataClass data identifier
     * @param key identifier
     * @param <K> identifier class
     */
    <K> void remove(Class dataClass, K key);

    /**
     * Remove all saved data by class.
     *
     * @param dataClass data class
     */
    void remove(Class dataClass);

    /**
     * Remove all data from all repositories.
     */
    void remove();

}
