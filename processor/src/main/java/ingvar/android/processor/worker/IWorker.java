package ingvar.android.processor.worker;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

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
     * If task with same key & cache class executing return future.
     *
     * @param task task
     * @param <R> result class
     * @return future of executing task, null otherwise
     */
    <R> Future<R> getExecuted(ITask task);

    /**
     * Creates and executes a one-shot action that becomes enabled after the given delay.
     *
     * @param task task
     * @param delay the time from now to delay execution (millis)
     * @param <R> result class
     * @return a ScheduledFuture that can be used to extract result or cancel
     */
    <R> ScheduledFuture<R> schedule(ITask task, long delay);

    /**
     * Creates and executes a periodic action that becomes enabled first after the given initial delay,
     * and subsequently with the given delay between the termination of one execution and the commencement of the next.
     * If any execution of the task encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or termination of the executor.
     *
     * @param task task
     * @param initialDelay the time to delay first execution (millis)
     * @param delay the delay between the termination of one execution and the commencement of the next.
     * @param <R> result class
     * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
     */
    <R> ScheduledFuture<R> schedule(ITask task, long initialDelay, long delay);

    /**
     * If task with same key & cache class scheduled return future.
     *
     * @param task task
     * @param <R> result class
     * @return future of executing task, null otherwise
     */
    <R> ScheduledFuture<R> getScheduled(ITask task);
    
}
