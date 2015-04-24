package ingvar.android.processor.util;

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

}
