package ingvar.android.processor.observation;

import android.app.Fragment;

import java.lang.ref.WeakReference;

import ingvar.android.processor.exception.ReferenceStaleException;

/**
 * Created by Igor Zubenko on 2015.05.04.
 */
public abstract class FragmentObserver<F extends Fragment, R> extends AbstractObserver<R> {

    private WeakReference<F> fragmentRef;

    public FragmentObserver(F fragment) {
        this.fragmentRef = new WeakReference<>(fragment);
    }

    /**
     * Get observer group
     *
     * @return fragment context class name
     */
    @Override
    public String getGroup() {
        return getFragment().getActivity().getClass().getName();
    }

    /**
     * Return reference to {@link Fragment}
     * @return fragment
     * @throws ReferenceStaleException if context is stale
     */
    protected F getFragment() {
        F fragment = fragmentRef.get();
        if(fragment == null) {
            throw new ReferenceStaleException("Fragment is stale!");
        }
        return fragment;
    }

    protected boolean isFragmentAdded() {
        return getFragment().isAdded();
    }

}
