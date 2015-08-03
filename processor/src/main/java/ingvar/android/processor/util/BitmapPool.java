package ingvar.android.processor.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Igor Zubenko on 2015.08.03.
 */
public class BitmapPool {

    private static class Holder {
        private static final BitmapPool INSTANCE = new BitmapPool();
    }

    private final Set<SoftReference<Bitmap>> reusableBitmaps;

    public static Bitmap decode(InputStream stream, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = width;
        options.outHeight = height;
        options.inSampleSize = 1;
        options.inMutable = true;
        options.inBitmap = Holder.INSTANCE.getBitmapFromReusableSet(options);

        return BitmapFactory.decodeStream(stream, null, options);
    }

    public static void free(Bitmap bitmap) {
        if(bitmap != null && bitmap.isMutable()) {
            Holder.INSTANCE.reusableBitmaps.add(new SoftReference<>(bitmap));
        }
    }

    protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (!reusableBitmaps.isEmpty()) {
            synchronized (reusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = reusableBitmaps.iterator();
                while (iterator.hasNext()) {
                    Bitmap item = iterator.next().get();
                    if (null != item && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap.
                        if (BitmapUtils.canUseForInBitmap(item, options)) {
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

    private BitmapPool() {
        reusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
    }
}
