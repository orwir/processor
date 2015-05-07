package ingvar.android.processor.examples.dictionary.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.14.
 */
public class CreateDictionaryTask extends SingleTask<String, Dictionary, SqliteSource> {

    public CreateDictionaryTask(String key) {
        super(key, Dictionary.class, SqliteSource.class, Time.ALWAYS_RETURNED);
    }

    @Override
    public Dictionary process(IObserverManager observerManager, SqliteSource source) {
        Dictionary newDictionary = new Dictionary(getTaskKey());
        ContentValues values = source.getConverter(Dictionary.class).convert(newDictionary);
        source.getContentResolver().insert(DictionaryContract.Dictionaries.CONTENT_URI, values);

        Uri query = new UriBuilder()
        .authority(DictionaryContract.AUTHORITY)
        .table(DictionaryContract.Dictionaries.TABLE_NAME)
        .where().eq(DictionaryContract.Dictionaries.Col.NAME, getTaskKey()).end()
        .build();

        Cursor cursor = source.getContentResolver().query(query, DictionaryContract.Dictionaries.PROJECTION, null, null, null);
        cursor.moveToFirst();
        Dictionary result = source.<Dictionary>getConverter(Dictionary.class).convert(cursor);
        cursor.close();

        return result;
    }

}
