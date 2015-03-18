package ingvar.android.processor.source;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface ISourceManager {

    Source get(Object key);

    void put(Object key, Source source);

    boolean isRegistered(Object key);

}
