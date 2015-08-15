package ingvar.android.processor.source;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of source manager.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.18.
 */
public class SourceManager implements ISourceManager {

    private Map<Class<? extends ISource>, ISource> sources;

    public SourceManager() {
        sources = new HashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends ISource> S getSource(Class<? extends ISource> key) {
        return (S) sources.get(key);
    }

    @Override
    public void addSource(Class<? extends ISource> key, ISource source) {
        sources.put(key, source);
    }

    @Override
    public boolean isRegistered(Class<? extends ISource> key) {
        return sources.get(key) != null;
    }

}
