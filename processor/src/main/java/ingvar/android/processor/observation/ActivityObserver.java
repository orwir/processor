package ingvar.android.processor.observation;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Created by Igor Zubenko on 2015.04.14.
 */
public abstract class ActivityObserver<A extends Activity, R> extends AbstractObserver<R> {

    private WeakReference<A> activityRef;

    public ActivityObserver(A activity) {
        activityRef = new WeakReference<>(activity);
    }

    @Override
    public String getGroup() {
        return getActivity().getClass().getName();
    }

    protected A getActivity() {
        A activity = activityRef.get();
        if(activity == null) {
            throw new IllegalStateException("Activity is stale!");
        }
        return activity;
    }

}
