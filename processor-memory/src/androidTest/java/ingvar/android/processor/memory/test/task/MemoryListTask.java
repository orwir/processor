package ingvar.android.processor.memory.test.task;

import java.util.ArrayList;
import java.util.List;

import ingvar.android.processor.memory.source.MemorySource;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.22.
 */
public class MemoryListTask extends SingleTask<Integer, List<String>, MemorySource> {

    public MemoryListTask(Integer key, long cacheExpirationTime) {
        super(key, String.class, MemorySource.class, cacheExpirationTime);
    }

    @Override
    public List<String> process(IObserverManager observerManager, MemorySource source) {
        List<String> result = new ArrayList<>();
        int count = getTaskKey();
        while(count --> 0) {
            result.add("result_value " + count);
        }
        return result;
    }

}
