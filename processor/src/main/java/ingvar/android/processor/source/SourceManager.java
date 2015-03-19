package ingvar.android.processor.source;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class SourceManager implements ISourceManager {

    private Map<Class<Source>, Source> sources;

    public SourceManager() {
        sources = new HashMap<>();
    }

    @Override
    public Source getSource(Class<Source> key) {
        return sources.get(key);
    }

    @Override
    public void addSource(Class key, Source source) {
        sources.put(key, source);
    }

    @Override
    public boolean isRegistered(Class<Source> key) {
        return sources.get(key) != null;
    }

}
