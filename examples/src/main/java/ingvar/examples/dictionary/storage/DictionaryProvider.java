package ingvar.examples.dictionary.storage;

import android.database.sqlite.SQLiteOpenHelper;

import ingvar.android.literepo.LiteProvider;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
public class DictionaryProvider extends LiteProvider {

    @Override
    protected SQLiteOpenHelper provideOpenHelper() {
        return new DictionaryHelper(getContext());
    }

}
