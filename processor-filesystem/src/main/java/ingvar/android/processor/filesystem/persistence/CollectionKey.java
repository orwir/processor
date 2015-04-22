package ingvar.android.processor.filesystem.persistence;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Igor Zubenko on 2015.04.22.
 */
public class CollectionKey<K> {

    private String prefix;
    private List<K> keys;

    public CollectionKey(String prefix, K... keys) {
        this.prefix = prefix;
        this.keys = Arrays.asList(keys);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<K> getKeys() {
        return keys;
    }

    public void setKeys(List<K> keys) {
        this.keys = keys;
    }

}
