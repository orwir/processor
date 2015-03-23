package ingvar.android.processor.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.request.IRequest;

/**
 *
 * Created by Igor Zubenko on 2015.03.19.
 */
public class Processor {

    private static final String TAG = Processor.class.getSimpleName();

    private Class<? extends ProcessorService> serviceClass;
    private ProcessorService service;
    private ServiceConnection connection;
    private Map<IRequest, IObserver[]> plannedRequests;

    public Processor(Class<? extends ProcessorService> serviceClass) {
        this.serviceClass = serviceClass;
        this.service = null;
        this.connection = new Connection();
        this.plannedRequests = new ConcurrentHashMap<>();
    }

    public <K, R> Future<R> execute(IRequest<K, R> request, IObserver<R>... observers) {
        if(service == null) {
            throw new ProcessorException("Service not bind yet");
        }
        return service.execute(request, observers);
    }

    public <K, R> void planExecute(IRequest<K, R> request, IObserver<R>... observers) {
        if(isBound()) {
            execute(request, observers);
        } else {
            plannedRequests.put(request, observers);
        }
    }

    public void bind(Context context) {
        Intent intent = new Intent(context, serviceClass);
        if(!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            throw new ProcessorException("Connection is not made. Maybe you forgot add your service to manifest?");
        }
    }

    public void unbind(Context context) {
        if(service != null) {
            service.removeObservers(context.getClass().getSimpleName());
        }
        context.unbindService(connection);
    }

    public boolean isBound() {
        return service != null;
    }

    private class Connection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, String.format("Service '%s' connected.", name));

            Processor.this.service = ((ProcessorService.ProcessorBinder) service).getService();

            if(plannedRequests.size() > 0) {
                Log.d(TAG, "Execute planned requests. Total: " + Integer.toString(plannedRequests.size()));

                for (Map.Entry<IRequest, IObserver[]> entry : plannedRequests.entrySet()) {
                    Processor.this.service.execute(entry.getKey(), entry.getValue());
                }
                plannedRequests.clear();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, String.format("Service '%s' disconnected.", name));

            Processor.this.service = null;
            plannedRequests.clear();
        }

    }

}
