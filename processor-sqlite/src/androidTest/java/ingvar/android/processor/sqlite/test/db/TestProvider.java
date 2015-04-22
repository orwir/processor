package ingvar.android.processor.sqlite.test.db;

import android.database.sqlite.SQLiteOpenHelper;

import ingvar.android.literepo.LiteProvider;

/**
 * Created by Igor Zubenko on 2015.04.22.
 */
public class TestProvider extends LiteProvider {

    @Override
    protected SQLiteOpenHelper provideOpenHelper() {
        return new TestOpenHelper(getContext());
    }

}
