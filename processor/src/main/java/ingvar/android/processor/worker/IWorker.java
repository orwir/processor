package ingvar.android.processor.worker;

import java.util.concurrent.Future;

import ingvar.android.processor.request.IRequest;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IWorker {

    <R> Future<R> execute(IRequest request);
    
}
