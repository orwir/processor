package ingvar.android.processor.util;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Log wrapper.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.05.11.
 */
public class LW {

    private static final Map<String, Integer> LEVEL = new ConcurrentHashMap<>();

    public static int v(String tag, String message, Object... args) {
        if(isLoggable(tag, Log.VERBOSE)) {
            return Log.v(tag, String.format(message, args));
        }
        return 0;
    }

    public static int d(String tag, String message, Object... args) {
        if(isLoggable(tag, Log.DEBUG)) {
            return Log.d(tag, String.format(message, args));
        }
        return 0;
    }

    public static int i(String tag, String message, Object... args) {
        if(isLoggable(tag, Log.INFO)) {
            return Log.i(tag, String.format(message, args));
        }
        return 0;
    }

    public static int w(String tag, String message, Object... args) {
        if(isLoggable(tag, Log.WARN)) {
            return Log.w(tag, String.format(message, args));
        }
        return 0;
    }

    public static int e(String tag, String message, Object... args) {
        if(isLoggable(tag, Log.ERROR)) {
            return Log.e(tag, String.format(message, args));
        }
        return 0;
    }

    public static int e(String tag, Throwable e) {
        if(isLoggable(tag, Log.ERROR)) {
            return Log.e(tag, e.getMessage(), e);
        }
        return 0;
    }

    public static int e(String tag, String message, Throwable e, Object... args) {
        if(isLoggable(tag, Log.ERROR)) {
            return Log.e(tag, String.format(message, args), e);
        }
        return 0;
    }

    public static boolean isLoggable(String tag, int level) {
        Integer lvl = LEVEL.get(tag);
        return level >= (lvl == null ? Log.DEBUG : lvl);
    }

    public static void setLevel(String tag, int level) {
        LEVEL.put(tag, level);
    }

    public static void resetLevels() {
        LEVEL.clear();
    }

    private LW() {}
}
