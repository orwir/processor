package ingvar.android.processor.test.task;

import junit.framework.TestCase;

import ingvar.android.processor.task.AbstractTask;
import ingvar.android.processor.task.ITask;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class AbstractTaskTest extends TestCase {

    public void testEquality() {
        ITask task = new DummyTask("task1", String.class);
        ITask result = new DummyTask("task1", Integer.class);
        ITask key = new DummyTask("task3", String.class);
        ITask mergeable = new DummyTask("task1", String.class);
        mergeable.setMergeable(false);


        assertFalse(task.equals(result));
        assertFalse(task.equals(key));
        assertFalse(task.equals(mergeable));
    }

    private class DummyTask extends AbstractTask {

        public DummyTask(Object key, Class resultClass) {
            super(key, resultClass);
        }

    }

}
