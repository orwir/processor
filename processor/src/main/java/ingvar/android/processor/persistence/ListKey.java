package ingvar.android.processor.persistence;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Igor Zubenko on 2015.04.22.
 */
@SuppressWarnings("unchecked")
public class ListKey<M, K> {

    private M major;
    private List<K> minors;

    public ListKey(M major, K... minors) {
        this.major = major;
        this.minors = Arrays.asList(minors);
    }

    public M getMajor() {
        return major;
    }

    public void setMajor(M major) {
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
        return 31 * major.hashCode() + 31 * minors.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof ListKey) {
            ListKey<M, K> o = (ListKey) object;
            return major.equals(o.major) && minors.equals(o.minors);
        }
        return false;
    }

}
