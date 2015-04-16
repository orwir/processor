package ingvar.android.processor.examples.dictionary.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.processor.examples.dictionary.persistence.MeaningConverter;
import ingvar.android.processor.examples.dictionary.pojo.Meaning;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.16.
 */
public class CreateMeaningTask extends SingleTask<Meaning, Meaning, SqliteSource> {

    public CreateMeaningTask(Meaning key) {
        super(key, Meaning.class, SqliteSource.class);
    }

    @Override
    public Meaning process(IObserverManager observerManager, SqliteSource source) {
        Meaning result = null;

        ContentResolver resolver = source.getContentResolver();
        MeaningConverter converter = (MeaningConverter) source.<Meaning>getConverter(Meaning.class);
        converter.setWord(getTaskKey().getWord());

        ContentValues values = converter.convert(getTaskKey());
        resolver.insert(DictionaryContract.Meanings.CONTENT_URI, values);

        Uri uri = new UriBuilder()
            .authority(DictionaryContract.AUTHORITY)
            .table(DictionaryContract.Meanings.TABLE_NAME)
            .eq(DictionaryContract.Meanings.Col.DICTIONARY_ID, getTaskKey().getWord().getDictionary().getId())
            .eq(DictionaryContract.Meanings.Col.WORD_ID, getTaskKey().getWord().getId())
            .eq(DictionaryContract.Meanings.Col.VALUE, getTaskKey().getValue())
        .build();
        Cursor cursor = resolver.query(uri, DictionaryContract.Meanings.PROJECTION, null, null, null);
        if(cursor.moveToFirst()) {
            result = converter.convert(cursor);
        }
        cursor.close();

        return result;
    }

}
