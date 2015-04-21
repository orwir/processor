package ingvar.android.processor.filesystem.persistence;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ingvar.android.processor.exception.PersistenceException;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class BitmapFilesystemRepository<K> extends FilesystemRepository<K, Bitmap> {

    private static final int DEFAULT_QUALITY = 100;

    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
    private BitmapFactory.Options decodingOptions = null;
    private int quality = DEFAULT_QUALITY;

    public BitmapFilesystemRepository(File directory, int maxBytes) {
        super(directory, maxBytes);
    }

    @Override
    public Bitmap persist(K key, Bitmap data) {
        String filename = filenameFromKey(key);

        BufferedOutputStream out = null;
        try {
            File file = storage.createEmptyFile(filename);
            out = new BufferedOutputStream(new FileOutputStream(file));

            boolean didCompress = data.compress(compressFormat, quality, out);
            if (!didCompress) {
                throw new PersistenceException(String.format("Could not compress bitmap for path: %s", file.getAbsolutePath()));
            }
            storage.register(file);

        } catch (IOException e) {
            throw new PersistenceException(e);
        } finally {
            if(out != null) {
                try {out.close();} catch (Exception e) {}
            }
        }

        return data;
    }

    @Override
    public Bitmap obtain(K key, long expiryTime) {
        String filename = filenameFromKey(key);

        Bitmap result = null;
        if(storage.contains(filename) && isNotExpired(key, expiryTime)) {
            File file = storage.getFile(filename);
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(file));
                result = BitmapFactory.decodeStream(is, null, decodingOptions);
            } catch (IOException e) {
                throw new PersistenceException(e);
            } finally {
                if(is != null) {
                    try {is.close();} catch (Exception e) {}
                }
            }
        }

        return result;
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return Bitmap.class.equals(dataClass);
    }

    public BitmapFactory.Options getDecodingOptions() {
        return decodingOptions;
    }

    public void setDecodingOptions(BitmapFactory.Options decodingOptions) {
        this.decodingOptions = decodingOptions;
    }

    public Bitmap.CompressFormat getCompressFormat() {
        return compressFormat;
    }

    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

}
