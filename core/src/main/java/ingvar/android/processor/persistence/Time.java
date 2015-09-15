package ingvar.android.processor.persistence;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class Time {

    /**
     * Never save and obtain result to cache.
     */
    public static long ALWAYS_EXPIRED = -1;
    /**
     * Always return result from cache if exists.
     */
    public static long ALWAYS_RETURNED = 0;

    /**
     * Update and save to cache.
     */
    public static long EXPIRED = 1;

    private Time() {}
}
