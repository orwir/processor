package ingvar.android.processor.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.request.IRequest;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class Processor {

    protected final long MAX_SECONDS_WAIT_BIND = TimeUnit.SECONDS.toMillis(10);

    private Class<? extends ProcessorService> serviceClass;
    private volatile ProcessorService service;
    private ServiceConnection connection;

    public Processor(Class<? extends ProcessorService> serviceClass) {
        this.serviceClass = serviceClass;
        this.service = null;
        this.connection = new Connection();
    }

    public <K, R> Future<R> execute(IRequest<K, R> request, IObserver<R>... observers) {
        if(service == null) {
            throw new ProcessorException("Service not bind yet");
        }
        return service.execute(request, observers);
    }

    public <K, R> Future<R> waitBindExecute(IRequest<K, R> request, IObserver<R>... observers) {
        long startTime = System.currentTimeMillis();
        while(service == null || (System.currentTimeMillis() - startTime) > maxWaitBind()) {
            try {Thread.sleep(100);} catch (InterruptedException e) {}
        }
        return execute(request, observers);
    }

    public void bind(Context context) {
        Intent intent = new Intent(context, serviceClass);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void unbind(Context context) {
        service.removeObservers(context.getClass().getSimpleName());
        context.unbindService(connection);
    }

    public boolean isBound() {
        return service != null;
    }

    protected long maxWaitBind() {
        return MAX_SECONDS_WAIT_BIND;
    }

    private class Connection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Processor.this.service = ((ProcessorService.ProcessorBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Processor.this.service = null;
        }

    }

}
