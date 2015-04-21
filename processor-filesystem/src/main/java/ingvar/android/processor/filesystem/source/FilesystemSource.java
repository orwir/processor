package ingvar.android.processor.filesystem.source;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

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
            try {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file, false), 1024);
                out.write(BytesUtils.toBytes(object));

                close(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
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
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(file));
                byte[] bytes = BytesUtils.streamToBytes(in);
                result = BytesUtils.fromBytes(bytes);
                close(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return (T) result;
    }

    public void close(Closeable closeable) {
        try {
            if(closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {}
    }

}
