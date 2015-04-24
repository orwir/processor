package ingvar.android.processor.test.worker;

import java.util.concurrent.Future;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.SingleTask;
import ingvar.android.processor.test.WorkerTest;
import ingvar.android.processor.test.mock.MockSource;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class DefaultWorkerTest extends WorkerTest {

    public void testExecute() throws Exception {
        SingleTask task = new DummyTask("test", 0);

        Future<String> result = getWorker().execute(task);

        assertEquals("Result incorrect", task.getTaskKey(), result.get());
    }

    public void testGetExecuted() throws Exception {
        SingleTask task = new DummyTask("test", 300);

        Future<String> future1 = getWorker().execute(task);
        Future<String> future2 = getWorker().getExecuted(task);

        assertSame("Executed future not match", future1, future2);
    }


    private class DummyTask extends SingleTask<String,String, MockSource> {

        private long timeout;

        public DummyTask(String key, long timeout) {
            super(key, String.class, MockSource.class);
            this.timeout = timeout;
        }

        @Override
        public String process(IObserverManager observerManager, MockSource source) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {}

            return getTaskKey();
        }

    }

}
