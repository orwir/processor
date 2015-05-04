package ingvar.android.processor.test.observation;

import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.observation.ContextObserver;
import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.task.SingleTask;
import ingvar.android.processor.test.mock.MockService;
import ingvar.android.processor.test.mock.MockSource;

/**
 * Created by Igor Zubenko on 2015.05.04.
 */
public class ObservationTest extends ServiceTestCase<MockService> {

    private static final String TEST_TASK_VALUE = "test_task_value_";
    private static final String VALUE_ERR = "ERR";
    private static final String VALUE_PROGRESS = "PROGRESS_";
    private static final String VALUE_CANCELLED = "CANCELLED";

    private Set<String> cache;

    public ObservationTest() {
        super(MockService.class);
        cache = new CopyOnWriteArraySet<>();
    }

    public void testCompleted() throws Exception {
        getService().execute(new TestTask(Time.ALWAYS_EXPIRED), new TestObserver(getContext()));

        while (!cache.contains(TEST_TASK_VALUE + "0")) {}
        assertTrue(cache.contains(TEST_TASK_VALUE + "0"));
    }

    public void testFailed() throws Exception {
        getService().execute(new TestTask(VALUE_ERR, Time.ALWAYS_EXPIRED), new TestObserver(getContext()));
        Thread.sleep(30);
        assertTrue(cache.contains(VALUE_ERR));
    }

    public void testProgress() throws Exception {
        getService().execute(new TestTask(Time.ALWAYS_EXPIRED), new TestObserver(getContext()));
        Thread.sleep(30);

        assertTrue(cache.contains(VALUE_PROGRESS + Float.toString(IObserver.MIN_PROGRESS)));
        assertTrue(cache.contains(VALUE_PROGRESS + Float.toString(IObserver.MAX_PROGRESS / 2)));
        assertTrue(cache.contains(VALUE_PROGRESS + Float.toString(IObserver.MAX_PROGRESS)));
    }

    public void testCancelled() throws Exception {
        TestTask task = new TestTask(Time.ALWAYS_EXPIRED);
        getService().execute(task, new TestObserver(getContext()));
        task.cancel();

        Thread.sleep(30);
        assertFalse(cache.contains(TEST_TASK_VALUE + "0"));
        assertTrue(cache.contains(VALUE_CANCELLED));
    }

    public void testReturnAndUpdate() throws Exception {
        /*TestTask task = new TestTask(5);

        getService().execute(task, new TestObserver(getContext()));
        Thread.sleep(30);
        assertTrue(cache.contains(TEST_TASK_VALUE + "0"));
        cache.clear();

        getService().execute(task, new TestObserver(getContext()));
        Thread.sleep(30);

        assertTrue(cache.contains(TEST_TASK_VALUE + "0"));
        assertTrue(cache.contains(TEST_TASK_VALUE + "1"));*/
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bindService(new Intent(getContext(), MockService.class));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(getService() != null) {
            getService().removeObservers(getContext());
            getService().clearCache();
        }
        if(cache != null) {
            cache.clear();
        }
    }

    private class TestTask extends SingleTask<String, String, MockSource> {

        private int counter;

        public TestTask(long cacheExpirationTime) {
            this(TEST_TASK_VALUE, cacheExpirationTime);
        }

        public TestTask(String key, long cacheExpirationTime) {
            super(key, String.class, MockSource.class, cacheExpirationTime);
        }

        @Override
        public synchronized String process(IObserverManager observerManager, MockSource source) {
            if(VALUE_ERR.equals(getTaskKey())) {
                throw new ProcessorException();
            }
            observerManager.notifyProgress(this, IObserver.MAX_PROGRESS / 2, null);
            return getTaskKey() + Integer.toString(counter++);
        }

    }

    private class TestObserver extends ContextObserver<Context, String> {

        public TestObserver(Context context) {
            super(context);
        }

        @Override
        public void completed(String result) {
            cache.add(result);
        }

        @Override
        public void failed(Exception exception) {
            cache.add(VALUE_ERR);
        }

        @Override
        public void progress(float progress, Map extra) {
            cache.add(VALUE_PROGRESS + Float.toString(progress));
        }

        @Override
        public void cancelled() {
            cache.add(VALUE_CANCELLED);
        }
    }

}
