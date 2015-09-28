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
    public static final int DEFAULT_CAPACITY = 15;

    private static class Holder {
        private static final PooledBitmapDecoder INSTANCE = new PooledBitmapDecoder();
    }

    private final Set<BitmapReference> reusableBitmaps;
    private int capacity;

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
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if(BitmapWatcher.isEnabled()) {
            BitmapWatcher.watch(bitmap);
        }
        return bitmap;
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
        Bitmap bitmap = BitmapFactory.decodeResource(resources, id, options);
        if(BitmapWatcher.isEnabled()) {
            BitmapWatcher.watch(bitmap);
        }
        return bitmap;
    }

    public static Bitmap decode(InputStream stream, BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if(options == null) {
            throw new NullPointerException("options can't be null!");
        }
        reqWidth = Math.max(0, reqWidth);
        reqHeight = Math.max(0, reqHeight);

        Bitmap bitmap = null;
        if(stream.markSupported()) {
            try {
                stream.mark(Integer.MAX_VALUE);
                if(options.outWidth <= 0 || options.outHeight <= 0) {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(stream, null, options);
                    stream.reset();
                }
                prepareOptionsForDecode(options, reqWidth, reqHeight);
                bitmap = BitmapFactory.decodeStream(stream, null, options);
            } catch (Throwable ignored) {}
        }
        if(bitmap == null) {
            bitmap = decode(BytesUtils.streamToBytes(stream), options, reqWidth, reqHeight);
        }
        if(BitmapWatcher.isEnabled()) {
            BitmapWatcher.watch(bitmap);
        }
        return bitmap;
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
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if(BitmapWatcher.isEnabled()) {
            BitmapWatcher.watch(bitmap);
        }
        return bitmap;
    }

    public static void free(Bitmap bitmap) {
        if(bitmap != null && bitmap.isMutable()) {
            PooledBitmapDecoder decoder = Holder.INSTANCE;
            if(decoder.reusableBitmaps.size() < decoder.capacity) {
                decoder.reusableBitmaps.add(new BitmapReference(bitmap));
            }
        }
    }

    public static void setCapacity(int capacity) {
        PooledBitmapDecoder decoder = Holder.INSTANCE;
        decoder.capacity = capacity;
        synchronized (decoder.reusableBitmaps) {
            while (decoder.reusableBitmaps.size() > decoder.capacity) {
                decoder.reusableBitmaps.iterator().remove();
            }
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
                final Iterator<BitmapReference> iterator = reusableBitmaps.iterator();
                while (iterator.hasNext()) {
                    BitmapReference reference = iterator.next();
                    Bitmap item = reference.get();
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
                    BitmapWatcher.vanish(reference.bitmapHashCode);
                }
            }
        }
        return bitmap;
    }

    private PooledBitmapDecoder() {
        reusableBitmaps = Collections.synchronizedSet(new HashSet<BitmapReference>());
        capacity = DEFAULT_CAPACITY;
    }

    private static class BitmapReference extends SoftReference<Bitmap> {

        private int bitmapHashCode;

        public BitmapReference(Bitmap r) {
            super(r);
            bitmapHashCode = r.hashCode();
        }

        public BitmapReference(int hashCode) {
            super(null, null);
            this.bitmapHashCode = hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BitmapReference that = (BitmapReference) o;
            return bitmapHashCode == that.bitmapHashCode;
        }

        @Override
        public int hashCode() {
            return bitmapHashCode;
        }

    }

}
