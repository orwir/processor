package ingvar.android.processor.memory.persistence;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import ingvar.android.processor.persistence.IRepository;
import ingvar.android.processor.util.PooledBitmapDecoder;

/**
 * Simple implementation memory LRU-cache for saving bitmaps.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.20.
 */
public class BitmapMemoryRepository<K> extends MemoryRepository<K, Bitmap> {

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

        @Override
        protected void entryRemoved(boolean evicted, K key, Entry<Bitmap> oldValue, Entry<Bitmap> newValue) {
            PooledBitmapDecoder.free(oldValue.getValue());
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
