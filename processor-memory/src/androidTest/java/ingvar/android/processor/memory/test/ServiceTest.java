package ingvar.android.processor.memory.test;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ServiceTestCase;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.memory.test.service.MockMemoryService;
import ingvar.android.processor.memory.test.task.MemoryListTask;
import ingvar.android.processor.memory.test.task.MemoryTask;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.source.ContextSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class ServiceTest extends ServiceTestCase<MockMemoryService> {

    public ServiceTest() {
        super(MockMemoryService.class);
    }

    public void testRequest() throws Exception {
        MemoryTask request = new MemoryTask("test", TimeUnit.HOURS.toMillis(1));
        Future<Object> future = getService().execute(request);

        assertEquals("Returned result not match", "test_value", future.get());
    }

    public void testRequestList() throws Exception {
        MemoryListTask request = new MemoryListTask(10, Time.ALWAYS_RETURNED);

        Future<List<String>> future = getService().execute(request);
        assertEquals(10, future.get().size());
        assertEquals(10, getService().<List<String>>obtainFromCache(10, String.class).size());
    }

    public void testCache() throws Exception {
        MemoryTask request = new MemoryTask("test2", TimeUnit.HOURS.toMillis(1));
        Future<Object> future = getService().execute(request);
        //wait until it saved
        future.get();

        Object cached = getService().obtainFromCache("test2", Object.class, Time.ALWAYS_RETURNED);
        assertEquals("Returned result not match", "test2_value", cached);
    }

    public void testAlwaysExpired() throws Exception {
        MemoryTask task = new MemoryTask("test_ae", Time.ALWAYS_EXPIRED);
        Future result = getService().execute(task);

        assertEquals("test_ae_value", result.get());
        assertNull(getService().obtainFromCache("test_ae", Object.class));
    }

    public void testAlwaysReturned() throws Exception {
        final String key = "test_ar";
        MemoryTask task = new MemoryTask(key, Time.ALWAYS_RETURNED);
        Future result = getService().execute(task);

        assertEquals("test_ar_value", result.get());
        assertEquals("test_ar_value", getService().obtainFromCache(key, Object.class));
        assertNull(getService().obtainFromCache(key, Object.class, Time.ALWAYS_EXPIRED));
    }

    public void testExpired() throws Exception {
        final String key = "test_e";
        MemoryTask task = new MemoryTask(key, Time.ALWAYS_RETURNED);
        Future result = getService().execute(task);

        assertEquals("test_e_value", result.get());
        assertEquals("test_e_value", getService().obtainFromCache(key, Object.class));
        Thread.sleep(150);
        assertNull(getService().obtainFromCache(key, Object.class, 100));
    }

    public void testBitmap() throws Exception {
        final String key = "hexapod.png";
        Bitmap expected = BitmapFactory.decodeStream(getContext().getAssets().open(key));
        Future<Bitmap> future = getService().execute(new BitmapTask(key));

        assertTrue(expected.sameAs(future.get()));
        assertTrue(expected.sameAs(getService().<Bitmap>obtainFromCache(key, Bitmap.class)));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bindService(new Intent(getContext(), MockMemoryService.class));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(getService() != null) {
            getService().removeObservers(getContext());
            getService().clearCache();
        }
    }

    private class BitmapTask extends SingleTask<String, Bitmap, ContextSource> {

        public BitmapTask(String key) {
            super(key, Bitmap.class, ContextSource.class, Time.ALWAYS_RETURNED);
        }

        @Override
        public Bitmap process(IObserverManager observerManager, ContextSource source) {
            try {
                return BitmapFactory.decodeStream(getContext().getAssets().open(getTaskKey()));
            } catch (IOException e) {
                throw new ProcessorException(e);
            }
        }

    }

}
