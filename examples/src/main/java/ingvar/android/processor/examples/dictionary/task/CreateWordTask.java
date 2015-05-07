package ingvar.android.processor.examples.dictionary.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.processor.examples.dictionary.persistence.WordConverter;
import ingvar.android.processor.examples.dictionary.pojo.Word;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.14.
 */
public class CreateWordTask extends SingleTask<Word, Word, SqliteSource> {

    public CreateWordTask(Word key) {
        super(key, Word.class, SqliteSource.class, Time.ALWAYS_EXPIRED);
    }

    @Override
    public Word process(IObserverManager observerManager, SqliteSource source) {
        Word result = null;

        ContentResolver resolver = source.getContentResolver();
        WordConverter converter = (WordConverter) source.<Word>getConverter(Word.class);
        converter.setDictionary(getTaskKey().getDictionary());

        ContentValues values = converter.convert(getTaskKey());
        resolver.insert(DictionaryContract.Words.CONTENT_URI, values);

        Uri uri = new UriBuilder()
        .authority(DictionaryContract.AUTHORITY)
        .table(DictionaryContract.Words.TABLE_NAME)
        .query()
            .eq(DictionaryContract.Words.Col.DICTIONARY_ID, getTaskKey().getDictionary().getId())
            .eq(DictionaryContract.Words.Col.VALUE, getTaskKey().getValue())
        .end()
        .build();
        Cursor cursor = resolver.query(uri, DictionaryContract.Words.PROJECTION, null, null, null);
        if(cursor.moveToFirst()) {
            result = converter.convert(cursor);
        }
        cursor.close();

        return result;
    }

}
