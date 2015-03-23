package ingvar.android.processor.util;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
public class CastUtils {

    public static <T> Class<T> cast(Class<?> clazz) {
        return (Class<T>) clazz;
    }

    private CastUtils() {}
}
