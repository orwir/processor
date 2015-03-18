package ingvar.android.processor.source;

import java.util.Map;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class SourceManager {

    private Map<Object, Source> sources;

    public Source get(Object key) {
        return sources.get(key);
    }

    public void put(Object key, Source source) {
        sources.put(key, source);
    }

    public boolean isRegistered(Object key) {
        return sources.get(key) != null;
    }

}
