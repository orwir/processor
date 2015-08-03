package ingvar.android.processor.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Igor Zubenko on 2015.08.03.
 */
public class PooledBitmapDecoder {

    public static final String TAG = PooledBitmapDecoder.class.getSimpleName();

    private static class Holder {
        private static final PooledBitmapDecoder INSTANCE = new PooledBitmapDecoder();
    }

    private final Set<SoftReference<Bitmap>> reusableBitmaps;

    //***********************PUBLIC STATIC METHODS (start)

    public static Bitmap decode(File file, BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if(options == null) {
            throw new NullPointerException("options can't be null!");
        }
        reqWidth = Math.max(0, reqWidth);
        reqHeight = Math.max(0, reqHeight);

        if(options.outWidth <= 0 || options.outHeight <= 0) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }
        prepareOptionsForDecode(options, reqWidth, reqHeight);
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static Bitmap decode(Resources resources, int id, BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if(options == null) {
            throw new NullPointerException("options can't be null!");
        }
        reqWidth = Math.max(0, reqWidth);
        reqHeight = Math.max(0, reqHeight);

        if(options.outWidth <= 0 || options.outHeight <= 0) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(resources, id, options);
        }
        prepareOptionsForDecode(options, reqWidth, reqHeight);
        return BitmapFactory.decodeResource(resources, id, options);
    }

    public static Bitmap decode(InputStream stream, BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if(options == null) {
            throw new NullPointerException("options can't be null!");
        }
        reqWidth = Math.max(0, reqWidth);
        reqHeight = Math.max(0, reqHeight);

        Bitmap result = null;
        if(stream.markSupported()) {
            try {
                stream.mark(Integer.MAX_VALUE);
                if(options.outWidth <= 0 || options.outHeight <= 0) {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(stream, null, options);
                    stream.reset();
                }
                prepareOptionsForDecode(options, reqWidth, reqHeight);
                result = BitmapFactory.decodeStream(stream, null, options);
            } catch (Throwable ignored) {}
        }
        if(result == null) {
            result = decode(BytesUtils.streamToBytes(stream), options, reqWidth, reqHeight);
        }
        return result;
    }

    public static Bitmap decode(byte[] data, BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if(options == null) {
            throw new NullPointerException("options can't be null!");
        }
        reqWidth = Math.max(0, reqWidth);
        reqHeight = Math.max(0, reqHeight);

        if(options.outWidth <= 0 || options.outHeight <= 0) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }
        prepareOptionsForDecode(options, reqWidth, reqHeight);
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static void free(Bitmap bitmap) {
        if(bitmap != null && bitmap.isMutable()) {
            Holder.INSTANCE.reusableBitmaps.add(new SoftReference<>(bitmap));
        }
    }

    //***********************PUBLIC STATIC METHODS (end)

    private static void prepareOptionsForDecode(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if(reqWidth > 0 || reqHeight > 0) {
            options.inSampleSize = BitmapUtils.calculateInSampleSize(options, reqWidth, reqHeight);
        } else {
            options.inSampleSize = 1;
        }
        options.inJustDecodeBounds = false;
        options.inMutable = true;
        options.inBitmap = Holder.INSTANCE.getBitmapFromReusableSet(options);
    }

    private Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (!reusableBitmaps.isEmpty()) {
            synchronized (reusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = reusableBitmaps.iterator();
                while (iterator.hasNext()) {
                    Bitmap item = iterator.next().get();
                    if (null != item && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap.
                        if (BitmapUtils.canUseForInBitmap(item, options)) {
                            LW.v(TAG, "Got bitmap from pool for %dx%d image.", options.outWidth, options.outHeight);
                            bitmap = item;
                            bitmap.eraseColor(Color.TRANSPARENT);
                            // Remove from reusable set so it can't be used again.
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    private PooledBitmapDecoder() {
        reusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
    }
}
