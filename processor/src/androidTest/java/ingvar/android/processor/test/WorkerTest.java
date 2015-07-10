package ingvar.android.processor.test;

import junit.framework.TestCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.observation.ObserverManager;
import ingvar.android.processor.persistence.CacheManager;
import ingvar.android.processor.persistence.ICacheManager;
import ingvar.android.processor.source.ISourceManager;
import ingvar.android.processor.source.SourceManager;
import ingvar.android.processor.task.DefaultWorker;
import ingvar.android.processor.task.IWorker;
import ingvar.android.processor.test.mock.MockSource;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public abstract class WorkerTest extends TestCase {

    private DefaultWorker worker;

    public WorkerTest() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        ICacheManager cacheManager = new CacheManager();
        ISourceManager sourceManager = new SourceManager();
        IObserverManager observerManager = new ObserverManager();

        sourceManager.addSource(MockSource.class, new MockSource());

        worker = new DefaultWorker.Builder()
        .setExecutorService(executor)
        .setScheduledService(scheduled)
        .setCacheManager(cacheManager)
        .setSourceManager(sourceManager)
        .setObserverManager(observerManager)
        .build();
    }

    protected IWorker getWorker() {
        return worker;
    }

}
