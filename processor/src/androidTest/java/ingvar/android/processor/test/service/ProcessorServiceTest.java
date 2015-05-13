package ingvar.android.processor.test.service;

import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;

import java.util.concurrent.Future;

import ingvar.android.processor.observation.ContextObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.task.ITask;
import ingvar.android.processor.task.SingleTask;
import ingvar.android.processor.test.mock.MockService;
import ingvar.android.processor.test.mock.MockSource;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class ProcessorServiceTest extends ServiceTestCase<MockService> {

    public ProcessorServiceTest() {
        super(MockService.class);
    }

    public void testExecute() throws Exception {
        ITask<Integer, Integer> task = new DummyTask(2048);

        Future<Integer> result = getService().execute(task, new DummyObserver(getContext()));

        assertEquals(1, getService().getObserverManager().getObservers(task).size());
        assertEquals(Integer.valueOf(2048), result.get());
    }

    public void testExecuteMergeable() throws Exception {
        ITask<Integer, Integer> task1 = new DummyTask(2048);
        ITask<Integer, Integer> task2 = new DummyTask(2048);

        Future<Integer> result1 = getService().execute(task1, new DummyObserver(getContext()));
        Future<Integer> result2 = getService().execute(task2, new DummyObserver(getContext()));

        assertEquals(1, getService().getObserverManager().getObservers().size());
        assertEquals(2, getService().getObserverManager().getObservers(task1).size());
        assertEquals(2, getService().getObserverManager().getObservers(task2).size());
        assertTrue(result1.get() == result2.get());
    }

    public void testExecuteNonMergeable() throws Exception {
        ITask<Integer, Integer> task1 = new DummyTask(2048);
        ITask<Integer, Integer> task2 = new DummyTask(2048);
        task2.setMergeable(false);

        Future<Integer> result1 = getService().execute(task1, new DummyObserver(getContext()));
        Future<Integer> result2 = getService().execute(task2, new DummyObserver(getContext()));

        assertEquals(2, getService().getObserverManager().getObservers().size());
        assertFalse(result1.get() == result2.get());
    }

    public void testObtainFromCache() throws Exception {
        ITask<Integer, Integer> task = new DummyTask(2048, Time.ALWAYS_RETURNED);

        Future<Integer> result = getService().execute(task);
        assertEquals(result.get(), obtainFromCache(2048, Integer.class, Time.ALWAYS_RETURNED));
    }

    public void testRemoveObserversByGroup() {
        ITask<Integer, Integer> task = new DummyTask(2048);
        getService().execute(task, new DummyObserver(getContext()));

        assertEquals(1, getService().getObserverManager().getObservers().size());
        getService().removeObservers(getContext());
        assertEquals(0, getService().getObserverManager().getObservers().size());
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
    }

    private <T> T obtainFromCache(Object key, Class dataClass, long cacheExpirationTime) {
        return getService().getCacheManager().obtain(key, dataClass, cacheExpirationTime);
    }

    private <T> T obtainFromCache(Object key, Class dataClass) {
        return obtainFromCache(key, dataClass, Time.ALWAYS_RETURNED);
    }

    private class DummyTask extends SingleTask<Integer, Integer, MockSource> {

        public DummyTask(Integer key) {
            super(key, MockSource.class);
        }

        public DummyTask(Integer key, long exipirationTime) {
            super(key, Integer.class, MockSource.class, exipirationTime);
        }

        @Override
        public Integer process(IObserverManager observerManager, MockSource source) {
            try {Thread.sleep(20);} catch (InterruptedException e) {}
            return new Integer(getTaskKey());
        }

    }

    private class DummyObserver extends ContextObserver {

        public DummyObserver(Context context) {
            super(context);
        }

        @Override
        public void completed(Object result) {

        }
    }

}
