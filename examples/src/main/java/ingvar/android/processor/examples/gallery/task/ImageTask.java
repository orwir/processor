package ingvar.android.processor.examples.gallery.task;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import ingvar.android.processor.examples.gallery.view.GalleryActivity;
import ingvar.android.processor.exception.PersistenceException;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
public class ImageTask extends SingleTask<String, Bitmap, FilesystemSource> {

    public ImageTask(String imageName) {
        super(imageName, Bitmap.class, FilesystemSource.class);
    }

    @Override
    public Bitmap process(IObserverManager observerManager, FilesystemSource source) {
        Bitmap result = null;
        AssetManager assets = source.getAssetManager();

        InputStream is = null;
        try {
            is = new BufferedInputStream(assets.open(GalleryActivity.ASSETS_DIR + "/" + getTaskKey()));
            result = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            throw new PersistenceException(e);
        } finally {
            source.close(is);
        }
        return result;
    }

}
