package ingvar.android.processor.source;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface ISourceManager {

    ISource getSource(Class<ISource> key);

    void addSource(Class<? extends ISource> key, ISource source);

    boolean isRegistered(Class<ISource> key);

}
