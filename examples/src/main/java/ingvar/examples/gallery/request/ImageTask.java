package ingvar.examples.gallery.request;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import ingvar.android.processor.exception.PersistenceException;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.filesystem.task.FilesystemTask;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.examples.gallery.view.GalleryActivity;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
public class ImageTask extends FilesystemTask<Bitmap> {

    public ImageTask(String imageName) {
        super(imageName, Bitmap.class, Time.ALWAYS_RETURNED);
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
            if(is != null) {
                try {is.close();} catch (Exception e) {}
            }
        }

        return result;
    }

}
