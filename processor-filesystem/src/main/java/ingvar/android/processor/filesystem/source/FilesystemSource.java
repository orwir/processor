package ingvar.android.processor.filesystem.source;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import ingvar.android.processor.filesystem.util.FileUtils;
import ingvar.android.processor.source.ContextSource;
import ingvar.android.processor.util.BytesUtils;

/**
 *
 * Created by Igor Zubenko on 2015.03.20.
 */
public class FilesystemSource extends ContextSource {

    public FilesystemSource(Context context) {
        super(context);
    }

    public AssetManager getAssetManager() {
        return getContext().getAssets();
    }

    public File save(String filename, Serializable object) {
        return save(new File(filename), object);
    }

    public File save(File file, Serializable object) {
        synchronized (file.getPath().intern()) {
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(file, false), 1024);
                out.write(BytesUtils.toBytes(object));

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                FileUtils.close(out);
            }
        }
        return file;
    }

    public <T> T load(String filename) {
        return load(new File(filename));
    }

    public <T> T load(File file) {
        Object result = null;
        if(file.exists() && file.isFile()) {
            InputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(file));
                byte[] bytes = BytesUtils.streamToBytes(in);
                result = BytesUtils.fromBytes(bytes);

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                FileUtils.close(in);
            }
        }
        return (T) result;
    }

}
