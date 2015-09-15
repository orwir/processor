package ingvar.android.processor.test.observation;

import junit.framework.TestCase;

import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.task.AbstractTask;
import ingvar.android.processor.task.ITask;
import ingvar.android.processor.test.mock.ExtendedObserverManager;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class ObserverManagerTest extends TestCase {

    private ExtendedObserverManager observerManager;

    public ObserverManagerTest() {
        observerManager = new ExtendedObserverManager();
    }

    public void testAddMergeable() throws Exception {
        ITask task1 = new DummyTask("task1", String.class);
        ITask task2 = new DummyTask("task1", String.class);

        observerManager.add(task1, new DummyObserver());
        assertEquals(1, observerManager.getObservers().size());
        assertEquals(1, observerManager.getObservers(task1).size());

        observerManager.add(task2, new DummyObserver());
        assertEquals(1, observerManager.getObservers().size());
        assertEquals(2, observerManager.getObservers(task1).size());
        assertEquals(2, observerManager.getObservers(task2).size());
    }

    public void testAddNonMergeable() {
        AbstractTask task1 = new DummyTask("task1", String.class);
        AbstractTask task2 = new DummyTask("task1", String.class);
        task2.setMergeable(false);

        IObserver observer1 = new DummyObserver();
        IObserver observer2 = new DummyObserver();

        observerManager.add(task1, observer1);
        assertEquals(1, observerManager.getObservers().size());
        assertEquals(1, observerManager.getObservers(task1).size());

        observerManager.add(task2, observer2);
        assertEquals(2, observerManager.getObservers().size());
        assertEquals(1, observerManager.getObservers(task1).size());
        assertEquals(1, observerManager.getObservers(task2).size());
    }


    private class DummyTask extends AbstractTask<String, Object> {

        public DummyTask(String key, Class resultClass) {
            super(key, resultClass);
        }

    }

    private class DummyObserver extends AbstractObserver {

        @Override
        public String getGroup() {
            return null;
        }

        @Override
        public void completed(Object result) {

        }

    }

}
