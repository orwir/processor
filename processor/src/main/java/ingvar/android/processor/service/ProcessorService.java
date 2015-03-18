package ingvar.android.processor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import ingvar.android.processor.worker.IWorker;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class ProcessorService extends Service {

    public class ProcessorBinder extends Binder {

        public ProcessorService getService() {
            return ProcessorService.this;
        }

    }

    private ProcessorBinder binder;
    private IWorker worker;

    public ProcessorService() {
        binder = new ProcessorBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
