package ingvar.android.processor.examples.dictionary.task;

import android.net.Uri;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.processor.examples.dictionary.pojo.Word;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.15.
 */
public class DeleteWordTask extends SingleTask<Word, String, SqliteSource> {

    public DeleteWordTask(Word key) {
        super(key, String.class, SqliteSource.class);
    }

    @Override
    public String process(IObserverManager observerManager, SqliteSource source) {
        Uri uri = new UriBuilder()
        .authority(DictionaryContract.AUTHORITY)
        .table(DictionaryContract.Words.TABLE_NAME)
        .where().eq(DictionaryContract.Words.Col._ID, getTaskKey().getId()).end()
        .build();
        source.getContentResolver().delete(uri, null, null);

        return getTaskKey().getValue();
    }

}
