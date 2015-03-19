package ingvar.android.processor.ram.source;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ingvar.android.processor.source.Source;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class RamSource implements Source {

    private Map<String, Object> externalStorage;

    public RamSource() {
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
