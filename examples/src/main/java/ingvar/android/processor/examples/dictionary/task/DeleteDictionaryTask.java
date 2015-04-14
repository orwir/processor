package ingvar.android.processor.examples.dictionary.task;

import android.net.Uri;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.14.
 */
public class DeleteDictionaryTask extends SingleTask<String, String, SqliteSource> {

    public DeleteDictionaryTask(String key) {
        super(key, String.class, SqliteSource.class, Time.ALWAYS_EXPIRED);
    }

    @Override
    public String process(IObserverManager observerManager, SqliteSource source) {
        Uri uri = new UriBuilder()
                .authority(DictionaryContract.AUTHORITY)
                .table(DictionaryContract.Dictionaries.TABLE_NAME)
                .eq(DictionaryContract.Dictionaries.Col.NAME, getTaskKey())
                .build();
        source.getContentResolver().delete(uri, null, null);
        return getTaskKey();
    }

}
