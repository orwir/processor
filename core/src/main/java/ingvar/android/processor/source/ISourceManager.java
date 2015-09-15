package ingvar.android.processor.source;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface ISourceManager {

    /**
     * Get source from source manager by source class.
     *
     * @param key source class
     * @return source
     */
    ISource getSource(Class<? extends ISource> key);

    /**
     * Add source to source manager.
     *
     * @param key source class
     * @param source source
     */
    void addSource(Class<? extends ISource> key, ISource source);

    /**
     * Check if source exists in the manager.
     *
     * @param key source class
     * @return true if exists, false otherwise
     */
    boolean isRegistered(Class<? extends ISource> key);

}
