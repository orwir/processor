package ingvar.android.processor.observation;

import android.content.Context;

import java.lang.ref.WeakReference;

import ingvar.android.processor.exception.ContextStaleException;

/**
 * Keeps a weak reference for {@link Context}
 *
 * <br/><br/>Created by Igor Zubenko on 2015.04.14.
 */
public abstract class ContextObserver<C extends Context, R> extends AbstractObserver<R> {

    private WeakReference<C> contextRef;

    public ContextObserver(C context) {
        contextRef = new WeakReference<>(context);
    }

    /**
     * Get observer group
     *
     * @return context class name
     */
    @Override
    public String getGroup() {
        return getContext().getClass().getName();
    }

    /**
     * Return reference to {@link Context}
     * @return context
     * @throws ingvar.android.processor.exception.ContextStaleException if context is stale
     */
    protected C getContext() {
        C context = contextRef.get();
        if(context == null) {
            throw new ContextStaleException("Context is stale!");
        }
        return context;
    }

}
