package ingvar.android.processor.network.source;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import ingvar.android.processor.source.ContextSource;

/**
 * Created by Igor Zubenko on 2015.03.24.
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

    public ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public NetworkInfo getNetworkInfo() {
        return getConnectivityManager().getActiveNetworkInfo();
    }

}
