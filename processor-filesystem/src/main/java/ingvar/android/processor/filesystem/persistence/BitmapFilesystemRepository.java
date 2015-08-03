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
import ingvar.android.processor.filesystem.util.FileUtils;

/**
 * Filesystem repository for caching bitmaps.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.20.
 */
public class BitmapFilesystemRepository<K> extends FilesystemRepository<K, Bitmap> {

    protected static final int DEFAULT_QUALITY = 100;

    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
    private BitmapFactory.Options decodingOptions = null;
    private int quality = DEFAULT_QUALITY;

    public BitmapFilesystemRepository(File directory, int maxBytes) {
        super(directory, maxBytes);
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return Bitmap.class.equals(dataClass);
    }

    /**
     * Get decoding options.
     *
     * @return decoding options
     */
    public BitmapFactory.Options getDecodingOptions() {
        return decodingOptions;
    }

    /**
     * Set decoding options.
     *
     * @param decodingOptions decoding options
     */
    public void setDecodingOptions(BitmapFactory.Options decodingOptions) {
        this.decodingOptions = decodingOptions;
    }

    /**
     * Get compress format.
     *
     * @return compress format
     */
    public Bitmap.CompressFormat getCompressFormat() {
        return compressFormat;
    }

    /**
     * Set compress format.
     *
     * @param compressFormat compress format
     */
    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    /**
     * Get quality.
     *
     * @return quality
     */
    public int getQuality() {
        return quality;
    }

    /**
     * Set quality.
     *
     * @param quality quality
     */
    public void setQuality(int quality) {
        this.quality = quality;
    }

    @Override
    protected Bitmap readFile(String filename) {
        Bitmap result = null;
        // TODO: 2015-08-03 use bitmap pool
        File file = storage.getFile(filename);
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            result = BitmapFactory.decodeStream(is, null, decodingOptions);
        } catch (IOException e) {
            throw new PersistenceException(e);
        } finally {
            FileUtils.close(is);
        }

        return result;
    }

    @Override
    protected void writeFile(String filename, Bitmap data) {
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
            FileUtils.close(out);
        }
    }

}
