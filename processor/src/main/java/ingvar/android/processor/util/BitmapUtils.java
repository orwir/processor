package ingvar.android.processor.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Created by Igor Zubenko on 2015.08.03.
 */
public class BitmapUtils {

    public static Bitmap tryGetBitmapFromDrawable(Drawable drawable) {
        if(drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }

    public static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / Math.max(1, targetOptions.inSampleSize);
            int height = targetOptions.outHeight / Math.max(1, targetOptions.inSampleSize);
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth
                && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            //https://github.com/square/picasso/blob/master/picasso/src/main/java/com/squareup/picasso/RequestHandler.java#L161
            if (reqHeight == 0) {
                inSampleSize = (int) Math.floor((float) width / (float) reqWidth);
            } else if (reqWidth == 0) {
                inSampleSize = (int) Math.floor((float) height / (float) reqHeight);
            } else {
                int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
                int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
                inSampleSize = Math.max(heightRatio, widthRatio);
            }
        }
        return inSampleSize;
    }

    /**
     * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
     */
    public static int getBytesPerPixel(Bitmap.Config config) {
        switch (config) {
            case ARGB_8888:
                return 4;
            case RGB_565:
            case ARGB_4444:
                return 2;
            case ALPHA_8:
            default:
                return 1;
        }
    }

    private BitmapUtils() {}
}
