package ingvar.android.processor.filesystem.persistence;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class BitmapFilesystemRepository extends FilesystemRepository<Bitmap> {

    public BitmapFilesystemRepository(File directory, int maxBytes) {
        super(directory, maxBytes);
    }

    @Override
    public Bitmap persist(String key, Bitmap data) {
        //TODO:
        //storage.put(key, data);
        return data;
    }

    @Override
    public Bitmap obtain(String key, long expiryTime) {
        //TODO:
        Bitmap result = null;
        if(storage.contains(key)) {
            long creationTime = storage.getTime(key);
            if((System.currentTimeMillis() - expiryTime) <= creationTime) {
                result = storage.get(key);
            }
        }
        return null;
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return Bitmap.class.equals(dataClass);
    }
}
