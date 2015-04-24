package ingvar.android.processor.examples.dictionary.task;

import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import ingvar.android.literepo.conversion.Converter;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.14.
 */
public class RequestDictionariesTask extends SingleTask<Uri, List<Dictionary>, SqliteSource> {

    public RequestDictionariesTask(Uri key) {
        super(key, Dictionary.class, SqliteSource.class, Time.ALWAYS_EXPIRED);
    }

    @Override
    public List<Dictionary> process(IObserverManager observerManager, SqliteSource source) {
        List<Dictionary> dictionaries = new ArrayList<>();

        Converter<Dictionary> converter = source.getConverter(Dictionary.class);
        Cursor cursor = source.getContentResolver().query(DictionaryContract.Dictionaries.CONTENT_URI,
                DictionaryContract.Dictionaries.PROJECTION, null, null, null);

        while(cursor.moveToNext()) {
            dictionaries.add(converter.convert(cursor));
        }
        cursor.close();

        return dictionaries;
    }

}
