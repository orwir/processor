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
import ingvar.android.processor.task.ITask;

/**
 *
 * Created by Igor Zubenko on 2015.03.19.
 */
public class Processor {

    private static final String TAG = Processor.class.getSimpleName();

    private Class<? extends ProcessorService> serviceClass;
    private ProcessorService service;
    private ServiceConnection connection;
    private Map<ITask, IObserver[]> plannedTasks;

    public Processor(Class<? extends ProcessorService> serviceClass) {
        this.serviceClass = serviceClass;
        this.service = null;
        this.connection = new Connection();
        this.plannedTasks = new ConcurrentHashMap<>();
    }

    public <K, R> Future<R> execute(ITask<K, R> task, IObserver<R>... observers) {
        if(service == null) {
            throw new ProcessorException("Service not bind yet");
        }
        return service.execute(task, observers);
    }

    public <K, R> void planExecute(ITask<K, R> task, IObserver<R>... observers) {
        if(isBound()) {
            execute(task, observers);
        } else {
            plannedTasks.put(task, observers);
        }
    }

    public void bind(Context context) {
        Log.d(TAG, String.format("Bind context '%s' to service '%s'", context.getClass().getSimpleName(), serviceClass.getSimpleName()));

        Intent intent = new Intent(context, serviceClass);
        if(!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            throw new ProcessorException("Connection is not made. Maybe you forgot add your service to AndroidManifest.xml?");
        }
    }

    public void unbind(Context context) {
        Log.d(TAG, String.format("Unbind context '%s' from service '%s'", context.getClass().getSimpleName(), serviceClass.getSimpleName()));

        if(service != null) {
            service.removeObservers(context.getClass().getName());
        }
        context.unbindService(connection);
        plannedTasks.clear();
        service = null;
    }

    public boolean isBound() {
        return service != null;
    }

    private class Connection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, String.format("Service '%s' connected.", name));

            Processor.this.service = ((ProcessorService.ProcessorBinder) service).getService();

            if(plannedTasks.size() > 0) {
                Log.d(TAG, "Execute planned tasks. Total: " + Integer.toString(plannedTasks.size()));

                for (Map.Entry<ITask, IObserver[]> entry : plannedTasks.entrySet()) {
                    Processor.this.service.execute(entry.getKey(), entry.getValue());
                }
                plannedTasks.clear();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, String.format("Service '%s' disconnected.", name));
            plannedTasks.clear();
            Processor.this.service = null;
        }

    }

}
