package ingvar.android.processor.util;

import android.graphics.Bitmap;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Igor Zubenko on 2015.09.28.
 */
public class BitmapWatcher {

    private static final String TAG = BitmapWatcher.class.getSimpleName();
    private static final long WAIT_TIMEOUT = 1000;

    private static class Holder {
        private static BitmapWatcher INSTANCE = new BitmapWatcher();
    }

    private static BitmapWatcher getInstance() {
        return Holder.INSTANCE;
    }

    public static void setEnabled(boolean enable) {
        BitmapWatcher watcher = getInstance();
        if(!enable && watcher.watchThread != null) {
            watcher.watchThread.interrupt();
        }
        if(!watcher.enabled && enable) {
            watcher.watchThread = watcher.new WatchThread();
            watcher.watchThread.start();
        }
        watcher.enabled = enable;
    }

    public static boolean isEnabled() {
        return getInstance().enabled;
    }

    public static void watch(Bitmap bitmap) {
        BitmapWatcher watcher = getInstance();
        if(watcher.enabled) {
            watcher.references.add(new BitmapReference(bitmap, watcher.referenceQueue));
            LW.v(TAG, "added new reference to watch.");
        } else {
            LW.e(TAG, "bitmap watcher not enabled!");
        }
    }

    private boolean enabled;
    private Set<BitmapReference> references;
    private ReferenceQueue<Bitmap> referenceQueue;
    private Thread watchThread;

    private BitmapWatcher() {
        enabled = false;
        references = Collections.synchronizedSet(new HashSet<BitmapReference>());
        referenceQueue = new ReferenceQueue<>();
        watchThread = null;
    }

    private static class BitmapReference extends PhantomReference<Bitmap> {

        private int hashCode;

        public BitmapReference(Bitmap r, ReferenceQueue<? super Bitmap> q) {
            super(r, q);
            hashCode = r.hashCode();
        }

        public BitmapReference(int hashCode) {
            super(null, null);
            this.hashCode = hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BitmapReference that = (BitmapReference) o;
            return hashCode == that.hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

    }

    private class WatchThread extends Thread {
        @Override
        public void run() {
            while (enabled) {
                try {
                    Bitmap bitmap = referenceQueue.remove(WAIT_TIMEOUT).get();
                    if(bitmap != null) {
                        PooledBitmapDecoder.free(bitmap);
                        references.remove(new BitmapReference(bitmap.hashCode()));
                    }
                } catch (InterruptedException interrupted) {
                    break;
                }
            }
        }
    }
}
