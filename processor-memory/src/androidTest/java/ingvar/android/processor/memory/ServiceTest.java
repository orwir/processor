package ingvar.android.processor.memory;

import android.content.Intent;
import android.test.ServiceTestCase;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ingvar.android.processor.memory.request.MemoryRequest;
import ingvar.android.processor.memory.service.MemoryProcessorService;
import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.service.ProcessorService;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class ServiceTest extends ServiceTestCase<MemoryProcessorService> {

    private Intent serviceIntent;
    private ProcessorService.ProcessorBinder binder;

    public ServiceTest() {
        super(MemoryProcessorService.class);
    }

    public void testRequest() throws Exception {
        MemoryRequest request = new MemoryRequest("test", TimeUnit.HOURS.toMillis(1));
        Future<Object> future = getService().execute(request);

        assertEquals("Returned result not match", "test_value", future.get());
    }

    public void testCache() throws Exception {
        MemoryRequest request = new MemoryRequest("test2", TimeUnit.HOURS.toMillis(1));
        Future<Object> future = getService().execute(request);
        //wait until it saved
        future.get();

        Object cached = getService().obtainFromCache("test2", Object.class, Time.ALWAYS_RETURNED);
        assertEquals("Returned result not match", "test_value_2", cached);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        serviceIntent = new Intent(getContext(), MemoryProcessorService.class);
        serviceIntent.putExtra(IObserver.KEY_GROUP, "ram-group");
        binder = (ProcessorService.ProcessorBinder) bindService(serviceIntent);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
