package ingvar.android.processor.test.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ingvar.android.processor.exception.TaskCancelledException;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.AggregatedTask;
import ingvar.android.processor.task.ITask;
import ingvar.android.processor.task.SingleTask;
import ingvar.android.processor.test.WorkerTest;
import ingvar.android.processor.test.mock.MockSource;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class AggregatedTaskTest extends WorkerTest {

    public void testSuccess() throws Exception {
        SumTask sum = new SumTask("sum = 10");
        String value = "1";
        for(int i = 0; i < 10; i++) {
            sum.addTask(new CalculationTask(value));
        }

        Future<Integer> result = getWorker().execute(sum);

        assertEquals("Result not match", Integer.valueOf(10), result.get());
    }

    public void testCancel() throws Exception {
        SumTask sum = new SumTask("sum = 10");
        String value = "1";
        for(int i = 0; i < 10; i++) {
            sum.addTask(new CalculationTask(value));
        }

        Future<TestType> result = getWorker().execute(sum);
        sum.cancel();
        try {
            result.get();
            assertFalse("Task was not cancelled!", true);
        } catch (ExecutionException e) {
            if(!(e.getCause() instanceof TaskCancelledException)) {
                throw e;
            }
        }
    }

    public void testInnerException() throws Exception {
        SumTask sum = new SumTask("sum = 10");
        String value = "abc";
        for(int i = 0; i < 10; i++) {
            sum.addTask(new CalculationTask(value));
        }

        Future<TestType> result = getWorker().execute(sum);

        result.get();
        assertTrue(sum.hasTasksExceptions());
        assertEquals("Exceptions count not match", sum.getTasks().size(), sum.getTasksExceptions().size());
    }

    private class SumTask extends AggregatedTask<String, Integer, Integer> {

        private Integer sum;

        public SumTask(String key) {
            super(key);
            sum = 0;
        }

        @Override
        public void onTaskComplete(ITask completed, Integer result) {
            sum += result;
        }

        @Override
        public Integer getCumulativeResult() {
            return sum;
        }

    }

    private class CalculationTask extends SingleTask<String, Integer, MockSource> {

        public CalculationTask(String key) {
            super(key, MockSource.class);
            setMergeable(false);
        }

        @Override
        public Integer process(IObserverManager observerManager, MockSource source) {
            try {Thread.sleep(5);} catch (InterruptedException e) {}
            return Integer.valueOf(getTaskKey());
        }

    }

}
