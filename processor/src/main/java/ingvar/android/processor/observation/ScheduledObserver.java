package ingvar.android.processor.observation;

import android.content.Context;

import java.lang.ref.WeakReference;

import ingvar.android.processor.util.CommonUtils;

/**
 * Created by Igor Zubenko on 2015.07.11.
 */
public abstract class ScheduledObserver<R> extends AbstractObserver<R> {

    private WeakReference<Context> contextRef;

    @Override
    public String getGroup() {
        try {
            return getContext().getClass().getName();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Observing cancellation event not supported for scheduling observer.
     */
    @Override
    public final void cancelled() {
        //nothing to do
    }

    public void setContext(Context context) {
        if(context == null) {
            contextRef = null;
        } else {
            contextRef = new WeakReference<>(context);
        }
    }

    public Context getContext() {
        if(contextRef == null) {
            throw new IllegalStateException("Reference to context is null!");
        }
        return CommonUtils.getReference(contextRef);
    }

}
