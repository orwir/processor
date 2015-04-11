package ingvar.android.processor.source;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
public class ContextSource implements ISource {

    private WeakReference<Context> contextRef;

    public ContextSource(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    public Context getContext() {
        Context context = contextRef.get();
        if(context == null) {
            throw new IllegalStateException("Context is stale");
        }
        return context;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

}
