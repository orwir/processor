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
import java.lang.ref.WeakReference;

import ingvar.android.processor.source.ISource;
import ingvar.android.processor.util.BytesUtils;

/**
 *
 * Created by Igor Zubenko on 2015.03.20.
 */
public class FilesystemSource implements ISource {

    private WeakReference<Context> contextRef;

    public FilesystemSource(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    public AssetManager getAssetManager() {
        Context context = contextRef.get();
        if(context == null) {
            throw new IllegalStateException("Context is stale!");
        }
        return context.getAssets();
    }

    public File save(String filename, Serializable object) {
        File file = new File(filename);
        synchronized (filename.intern()) {
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
        Object result = null;
        File file = new File(filename);
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

    @Override
    public boolean isAvailable() {
        return true;
    }

    private void close(Closeable closeable) {
        try {
            if(closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {}
    }

}
