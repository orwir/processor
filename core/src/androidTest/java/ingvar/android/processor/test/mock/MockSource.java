package ingvar.android.processor.test.mock;

import ingvar.android.processor.source.ISource;

/**
 * Created by Igor Zubenko on 2015.04.20.
 */
public class MockSource implements ISource {

    @Override
    public boolean isAvailable() {
        return true;
    }

}
