package ingvar.android.processor.network.source;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;

import ingvar.android.processor.source.ISource;

/**
 * Created by Igor Zubenko on 2015.03.24.
 */
public class NetworkSource implements ISource {

    private WeakReference<Context> contextRef;

    public NetworkSource(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    @Override
    public boolean isAvailable() {
        NetworkInfo network = getNetworkInfo();
        return network != null && network.isConnectedOrConnecting();
    }

    public Context getContext() {
        Context context = contextRef.get();
        if(context == null) {
            throw new IllegalStateException("Context is stale!");
        }
        return context;
    }

    public ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public NetworkInfo getNetworkInfo() {
        return getConnectivityManager().getActiveNetworkInfo();
    }

}
