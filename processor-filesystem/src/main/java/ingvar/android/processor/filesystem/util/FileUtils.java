package ingvar.android.processor.filesystem.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Igor Zubenko on 2014.09.04.
 */
public class FileUtils {

    public static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private FileUtils() {}

}
