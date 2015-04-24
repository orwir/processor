package ingvar.android.processor.persistence;

import java.util.Arrays;
import java.util.List;

import ingvar.android.processor.exception.PersistenceException;

import static ingvar.android.processor.util.CommonUtils.isEquals;
import static ingvar.android.processor.util.CommonUtils.objectHashCode;

/**
 * Key for persisting/obtaining collections of data.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.04.22.
 *
 * @param <K> keys class
 */
public class CompositeKey<K> {

    private K major;
    private List<K> minors;

    public CompositeKey(K major, K... minors) {
        this.major = major;
        this.minors = Arrays.asList(minors);
        if(major == null && minors.length == 0) {
            throw new PersistenceException("At least one part of key must not be null!");
        }
    }

    /**
     * Get common part for keys.
     *
     * @return major key part
     */
    public K getMajor() {
        return major;
    }

    /**
     * Set common part for keys.
     *
     * @param major major key part
     */
    public void setMajor(K major) {
        this.major = major;
    }

    /**
     * Get concrete parts for single items.
     *
     * @return minor key parts
     */
    public List<K> getMinors() {
        return minors;
    }

    /**
     * Set concrete parts for single items.
     *
     * @param minors minor key parts
     */
    public void setMinors(List<K> minors) {
        this.minors = minors;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode *= 31 + objectHashCode(major);
        hashCode *= 31 + objectHashCode(minors);
        return hashCode;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof CompositeKey) {
            CompositeKey o = (CompositeKey) object;
            return isEquals(major, o.major) && isEquals(minors, o.minors);
        }
        return false;
    }

}
