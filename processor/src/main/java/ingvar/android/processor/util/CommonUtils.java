package ingvar.android.processor.util;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;

import ingvar.android.processor.exception.ReferenceStaleException;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class CommonUtils {

    /**
     * Compare two objects for equality.
     *
     * @param o1 one object
     * @param o2 another object
     * @return true if equals, false otherwise
     */
    public static boolean isEquals(Object o1, Object o2) {
        return (o1 == o2) || (o1 != null && o1.equals(o2));
    }

    /**
     * Hash code of object.
     *
     * @param object object
     * @return hash code or 0 if object is null
     */
    public static int objectHashCode(Object object) {
        return object == null ? 0 : object.hashCode();
    }

    public static boolean isNull(Object value) {
        if(value instanceof String) {
            return ((String) value).isEmpty();
        }
        else if(value instanceof Collection) {
            return ((Collection) value).isEmpty();
        }
        else if(value instanceof Map) {
            return ((Map) value).isEmpty();
        }
        return value == null;
    }

    public static boolean isNotNull(Object value) {
        return !isNull(value);
    }

    public static  <T> T getReference(WeakReference<T> weak) {
        T reference = weak.get();
        if(reference == null) {
            throw new ReferenceStaleException("Reference is stale!");
        }
        return reference;
    }

}
