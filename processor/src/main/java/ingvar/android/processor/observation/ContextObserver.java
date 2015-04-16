package ingvar.android.processor.observation;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Created by Igor Zubenko on 2015.04.14.
 */
public abstract class ContextObserver<C extends Context, R> extends AbstractObserver<R> {

    private WeakReference<C> contextRef;

    public ContextObserver(C context) {
        contextRef = new WeakReference<>(context);
    }

    @Override
    public String getGroup() {
        return getContext().getClass().getName();
    }

    protected C getContext() {
        C context = contextRef.get();
        if(context == null) {
            throw new IllegalStateException("Context is stale!");
        }
        return context;
    }

}
