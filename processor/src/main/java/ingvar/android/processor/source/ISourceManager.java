package ingvar.android.processor.source;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface ISourceManager {

    Source getSource(Class<Source> key);

    void addSource(Class<? extends Source> key, Source source);

    boolean isRegistered(Class<Source> key);

}
