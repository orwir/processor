package ingvar.android.processor.test;

import junit.framework.TestCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.observation.ObserverManager;
import ingvar.android.processor.persistence.CacheManager;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.source.ISourceManager;
import ingvar.android.processor.source.SourceManager;
import ingvar.android.processor.test.mock.MockSource;
import ingvar.android.processor.worker.DefaultWorker;
import ingvar.android.processor.worker.IWorker;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public abstract class WorkerTest extends TestCase {

    private DefaultWorker worker;

    public WorkerTest() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ICacheManager cacheManager = new CacheManager();
        ISourceManager sourceManager = new SourceManager();
        IObserverManager observerManager = new ObserverManager();

        sourceManager.addSource(MockSource.class, new MockSource());

        worker = new DefaultWorker(executor, cacheManager, sourceManager, observerManager);
    }

    protected IWorker getWorker() {
        return worker;
    }

}
