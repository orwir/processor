package ingvar.android.processor.filesystem.util;

import android.os.Environment;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Igor Zubenko on 2014.09.04.
 */
public class FileUtils {

    /**
     * Checks if external storage is available for read and write
     *
     * @return true if external storage available, false otherwise
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     *
     * @return true if external storage readable, false otherwise
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Silent close closeable instance
     *
     * @param closeable instance
     */
    public static void close(Closeable closeable) {
        try {
            if(closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {}
    }

    private FileUtils() {}

}
