package ingvar.android.processor.network.source;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.io.InputStream;

import ingvar.android.processor.source.ContextSource;

/**
 * Base implementation of network source.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.24.
 */
public class NetworkSource extends ContextSource {

    public NetworkSource(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        NetworkInfo network = getNetworkInfo();
        return network != null && network.isConnectedOrConnecting();
    }

    public InputStream download(Uri uri) {
        throw new UnsupportedOperationException("Stub! Override it!");
    }

    /**
     * Get connectivity manager.
     *
     * @return manager
     */
    public ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Get network info.
     *
     * @return network info
     */
    public NetworkInfo getNetworkInfo() {
        return getConnectivityManager().getActiveNetworkInfo();
    }

}
