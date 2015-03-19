package ingvar.android.processor.memory.source;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ingvar.android.processor.source.ISource;

/**
 * FOR TESTS ONLY!
 *
 * Created by Igor Zubenko on 2015.03.19.
 */
public class MemorySource implements ISource {

    private Map<String, Object> externalStorage;

    public MemorySource() {
        externalStorage = new ConcurrentHashMap<>();
        externalStorage.put("test", "test_value");
        externalStorage.put("test2", "test_value_2");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public Object get(String key) {
        return externalStorage.get(key);
    }

}
