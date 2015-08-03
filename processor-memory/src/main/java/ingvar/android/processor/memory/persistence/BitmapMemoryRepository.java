package ingvar.android.processor.memory.persistence;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import ingvar.android.processor.persistence.IRepository;

/**
 * Simple implementation memory LRU-cache for saving bitmaps.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.20.
 */
public class BitmapMemoryRepository<K> extends MemoryRepository<K, Bitmap> {
    // TODO: 2015-08-03 use bitmap pool
    private static class BitmapLruCache<K> extends LruCache<K, Entry<Bitmap>> {

        public BitmapLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(K key, Entry<Bitmap> entry) {
            int size;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                size = entry.getValue().getAllocationByteCount();
            } else {
                size = entry.getValue().getByteCount();
            }
            return size;
        }
    }

    @SuppressWarnings("unchecked")
    public BitmapMemoryRepository(int maxSize, IRepository decorated) {
        super((LruCache) new BitmapLruCache<Bitmap>(maxSize), decorated);
    }

    @SuppressWarnings("unchecked")
    public BitmapMemoryRepository(int maxSize) {
        super((LruCache) new BitmapLruCache<Bitmap>(maxSize));
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return Bitmap.class.equals(dataClass);
    }

}
