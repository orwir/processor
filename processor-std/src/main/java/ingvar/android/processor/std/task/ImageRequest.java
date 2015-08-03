package ingvar.android.processor.std.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.InputStream;

import ingvar.android.processor.filesystem.util.FileUtils;
import ingvar.android.processor.network.source.NetworkSource;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.05.01.
 */
public class ImageRequest extends SingleTask<Uri, Bitmap, NetworkSource> {

    public ImageRequest(Uri uri) {
        super(uri, Bitmap.class, NetworkSource.class, Time.ALWAYS_RETURNED);
    }

    public ImageRequest(String uri) {
        this(Uri.parse(uri));
    }

    @Override
    public Bitmap process(IObserverManager observerManager, NetworkSource source) {
        if(source.isAvailable()) {
            InputStream is = source.download(getTaskKey());
            // TODO: 2015-08-03 use bitmap pool
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            FileUtils.close(is);
            return bitmap;
        }
        throw new IllegalStateException("Network is not available!");
    }

}
