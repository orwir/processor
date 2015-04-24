package ingvar.android.processor.persistence;

import java.util.Arrays;
import java.util.List;

import ingvar.android.processor.exception.PersistenceException;

import static ingvar.android.processor.util.CommonUtils.isEquals;
import static ingvar.android.processor.util.CommonUtils.objectHashCode;

/**
 * Created by Igor Zubenko on 2015.04.22.
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

    public K getMajor() {
        return major;
    }

    public void setMajor(K major) {
        this.major = major;
    }

    public List<K> getMinors() {
        return minors;
    }

    public void setMinors(List<K> minors) {
        this.minors = minors;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += objectHashCode(major) * 31;
        hashCode += objectHashCode(minors) * 31;
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
