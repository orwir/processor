package ingvar.android.processor.sqlite.source;

import android.content.ContentResolver;
import android.content.Context;

import java.lang.ref.WeakReference;

import ingvar.android.processor.source.ISource;

/**
 * Created by Igor Zubenko on 2015.04.07.
 */
public class SqliteSource implements ISource {

    private WeakReference<Context> contextRef;

    public SqliteSource(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public Context getContext() {
        Context context = contextRef.get();
        if(context == null) {
            throw new IllegalStateException("Context is stale!");
        }
        return context;
    }

    public ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }

}
