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
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.task.ITask;

/**
 * Wrapper for processing service.
 * Just provide helper methods.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.19.
 */
public class Processor<S extends ProcessorService> {

    private static final String TAG = Processor.class.getSimpleName();

    private Class<? extends ProcessorService> serviceClass;
    private Map<ITask, IObserver[]> plannedTasks;
    private ServiceConnection connection;
    private S service;

    public Processor(Class<? extends ProcessorService> serviceClass) {
        this.serviceClass = serviceClass;
        this.service = null;
        this.connection = new Connection();
        this.plannedTasks = new ConcurrentHashMap<>();
    }

    /**
     * Send task for execution.
     *
     * @param task task
     * @param observers task observers
     * @param <K> task identifier class
     * @param <R> task result class
     * @return {@link Future} of task execution
     */
    public <K, R> Future<R> execute(ITask<K, R> task, IObserver<R>... observers) {
        if(service == null) {
            throw new ProcessorException("Service is not bound yet!");
        }
        return service.execute(task, observers);
    }

    /**
     * If service is bound execute task, otherwise add to queue.
     *
     * @param task task
     * @param observers task observers
     * @param <K> task identifier class
     * @param <R> task result class
     */
    public <K, R> void planExecute(ITask<K, R> task, IObserver<R>... observers) {
        if(isBound()) {
            execute(task, observers);
        } else {
            plannedTasks.put(task, observers);
        }
    }

    /**
     * Remove registered observers from task.
     *
     * @param task task
     */
    public void removeObservers(ITask task) {
        service.getObserverManager().remove(task);
    }

    /**
     * Obtain task result from cache.
     *
     * @param key result identifier
     * @param dataClass single result item class
     * @param expiryTime how much time data consider valid in the repository
     * @param <R> returned result class
     * @return cached result if exists and did not expired, null otherwise
     */
    public <R> R obtainFromCache(Object key, Class dataClass, long expiryTime) {
        return service.getCacheManager().obtain(key, dataClass, expiryTime);
    }

    /**
     * Obtain task result from cache if exists.
     *
     * @param key result identifier
     * @param dataClass single result item class
     * @param <R> returned result class
     * @return cached result if exists, null otherwise
     */
    public <R> R obtainFromCache(Object key, Class dataClass) {
        return obtainFromCache(key, dataClass, Time.ALWAYS_RETURNED);
    }

    public void removeFromCache(Object key, Class dataClass) {
        service.getCacheManager().remove(key, dataClass);
    }

    /**
     * Remove all data by class.
     *
     * @param dataClass data class
     */
    public void clearCache(Class dataClass) {
        service.getCacheManager().remove(dataClass);
    }

    /**
     * Remove all data from cache.
     */
    public void clearCache() {
        service.clearCache();
    }

    /**
     * Bind service to context.
     *
     * @param context context
     */
    public void bind(Context context) {
        Log.d(TAG, String.format("Bind context '%s' to service '%s'", context.getClass().getSimpleName(), serviceClass.getSimpleName()));

        Intent intent = new Intent(context, serviceClass);
        if(!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            throw new ProcessorException("Connection is not made. Maybe you forgot add your service to AndroidManifest.xml?");
        }
    }

    /**
     * Unbind service from context.
     * Remove all planned tasks if exist.
     *
     * @param context
     */
    public void unbind(Context context) {
        Log.d(TAG, String.format("Unbind context '%s' from service '%s'", context.getClass().getSimpleName(), serviceClass.getSimpleName()));

        if(service != null) {
            service.removeObservers(context);
        }
        context.unbindService(connection);
        plannedTasks.clear();
        service = null;
    }

    /**
     * Check bound service or not.
     *
     * @return true if bound, false otherwise
     */
    public boolean isBound() {
        return service != null;
    }

    /**
     * Get service.
     *
     * @return service or null if not bound
     */
    public S getService() {
        return service;
    }

    private class Connection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, String.format("Service '%s' connected.", name));

            Processor.this.service = (S) ((ProcessorService.ProcessorBinder) service).getService();

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
