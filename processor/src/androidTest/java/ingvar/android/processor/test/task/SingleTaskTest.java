package ingvar.android.processor.test.task;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.AbstractTask;
import ingvar.android.processor.task.Execution;
import ingvar.android.processor.task.SingleTask;
import ingvar.android.processor.test.WorkerTest;
import ingvar.android.processor.test.mock.MockSource;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class SingleTaskTest extends WorkerTest {

    public void testSuccess() throws Exception {
        AbstractTask task = new Task(TestType.SUCCESS);
        Execution result = getWorker().execute(task);

        assertEquals("Task process was not successful", task.getTaskKey(), result.getFuture().get());
    }

    public void testCancel() throws Exception {
        AbstractTask task = new Task(TestType.CANCEL);
        Execution result = getWorker().execute(task);
        task.cancel();
        try {
            result.getFuture().get();
            assertFalse("Task was not cancelled!", true);
        } catch (CancellationException ignored) {}
    }

    public void testException() throws Exception {
        AbstractTask task = new Task(TestType.EXCEPTION);
        Execution result = getWorker().execute(task);

        try {
            result.getFuture().get();
            assertFalse("Task was not throw exception!", true);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof ProcessorException) {
                if(!"Test exception!".equals(e.getCause().getMessage())) {
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }


    private class Task extends SingleTask<TestType, TestType, MockSource> {

        public Task(TestType type) {
            super(type, MockSource.class);
        }

        @Override
        public TestType process(IObserverManager observerManager, MockSource source) {
            switch (getTaskKey()) {
                case SUCCESS:
                    //nothing to do
                    break;
                case CANCEL:
                    try {Thread.sleep(200);} catch (InterruptedException e) {}
                    break;
                case EXCEPTION:
                    throw new ProcessorException("Test exception!");
            }
            return getTaskKey();
        }

    }

}
