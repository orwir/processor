package ingvar.android.processor.util;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class CommonUtils {

    public static boolean isEquals(Object o1, Object o2) {
        return (o1 == o2) || (o1 != null && o1.equals(o2));
    }

    public static int objectHashCode(Object object) {
        return object == null ? 0 : object.hashCode();
    }

}
