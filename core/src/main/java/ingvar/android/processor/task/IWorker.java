package ingvar.android.processor.task;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IWorker {

    /**
     * Execute task.
     *
     * @param task task
     * @return execution
     */
    Execution execute(AbstractTask task);

    /**
     * Creates and executes a one-shot action that becomes enabled after the given delay.
     *
     * @param task task
     * @param delay the time from now to delay execution (millis)
     * @return a ScheduledExecution that can be used to extract result or cancel
     */
    ScheduledExecution schedule(AbstractTask task, long delay);

    /**
     * Creates and executes a periodic action that becomes enabled first after the given initial delay,
     * and subsequently with the given delay between the termination of one execution and the commencement of the next.
     * If any execution of the task encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or termination of the executor.
     *
     * @param task task
     * @param initialDelay the time to delay first execution (millis)
     * @param delay the delay between the termination of one execution and the commencement of the next.
     * @return a ScheduledExecution representing pending completion of the task, and whose get() method will throw an exception upon cancellation
     */
    ScheduledExecution schedule(AbstractTask task, long initialDelay, long delay);

    /**
     * If task with same key & cache class executing return execution.
     *
     * @param task task
     * @return execution or null otherwise
     */
    Execution getExecuted(AbstractTask task);

    /**
     * If task with same key & cache class scheduled return execution.
     *
     * @param task task
     * @return future of executing task, null otherwise
     */
    ScheduledExecution getScheduled(AbstractTask task);
    
}
