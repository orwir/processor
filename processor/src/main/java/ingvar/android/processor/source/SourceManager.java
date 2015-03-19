package ingvar.android.processor.source;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class SourceManager implements ISourceManager {

    private Map<Class<ISource>, ISource> sources;

    public SourceManager() {
        sources = new HashMap<>();
    }

    @Override
    public ISource getSource(Class<ISource> key) {
        return sources.get(key);
    }

    @Override
    public void addSource(Class key, ISource source) {
        sources.put(key, source);
    }

    @Override
    public boolean isRegistered(Class<ISource> key) {
        return sources.get(key) != null;
    }

}
