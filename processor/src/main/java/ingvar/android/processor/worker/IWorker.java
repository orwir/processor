package ingvar.android.processor.worker;

import java.util.concurrent.Future;

import ingvar.android.processor.task.ITask;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IWorker {

    /**
     * Execute task.
     *
     * @param task task
     * @param <R> result class
     * @return future of execution
     */
    <R> Future<R> execute(ITask task);

    /**
     * If task with same key executing return future.
     *
     * @param task task
     * @param <R> result class
     * @return future of executing task, null otherwise
     */
    <R> Future<R> getExecuted(ITask task);
    
}
