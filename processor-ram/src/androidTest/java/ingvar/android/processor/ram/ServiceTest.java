package ingvar.android.processor.ram;

import android.content.Intent;
import android.test.ServiceTestCase;

import java.util.concurrent.Future;

import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.ram.request.RamRequest;
import ingvar.android.processor.ram.service.RamProcessorService;
import ingvar.android.processor.service.ProcessorService;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class ServiceTest extends ServiceTestCase<RamProcessorService> {

    private Intent serviceIntent;
    private ProcessorService.ProcessorBinder binder;

    public ServiceTest() {
        super(RamProcessorService.class);
    }

    public void testRequest() throws Exception {
        RamRequest request = new RamRequest("test");
        Future<Object> future = getService().execute(request);

        assertEquals("Returned result not match", "test_value", future.get());
    }

    public void testCache() throws Exception {
        RamRequest request = new RamRequest("test2");
        Future<Object> future = getService().execute(request);
        //wait until it saved
        future.get();

        Object cached = getService().obtainFromCache("test2", Object.class, Time.ALWAYS_RETURNED);
        assertEquals("Returned result not match", "test_value_2", cached);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        serviceIntent = new Intent(getContext(), RamProcessorService.class);
        serviceIntent.putExtra(IObserver.KEY_GROUP, "ram-group");
        binder = (ProcessorService.ProcessorBinder) bindService(serviceIntent);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
