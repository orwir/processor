package ingvar.android.processor.examples.dictionary.task;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.processor.examples.dictionary.persistence.WordConverter;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;
import ingvar.android.processor.examples.dictionary.pojo.Word;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.14.
 */
public class RequestWordsTask extends SingleTask<Dictionary, List<Word>, SqliteSource> {

    public RequestWordsTask(Dictionary key, boolean refresh) {
        super(key, Word.class, SqliteSource.class, refresh ? Time.ALWAYS_EXPIRED : Time.ALWAYS_RETURNED);
    }

    @Override
    public List<Word> process(IObserverManager observerManager, SqliteSource source) {
        List<Word> words = new ArrayList<>();

        ContentResolver resolver = source.getContentResolver();
        WordConverter converter = (WordConverter) source.<Word>getConverter(Word.class);
        converter.setDictionary(getTaskKey());

        Uri uri = new UriBuilder()
        .authority(DictionaryContract.AUTHORITY)
        .table(DictionaryContract.Words.TABLE_NAME)
        .where().eq(DictionaryContract.Words.Col.DICTIONARY_ID, getTaskKey().getId()).end()
        .build();
        Cursor cursor = resolver.query(uri, DictionaryContract.Words.PROJECTION, null, null, DictionaryContract.Words.SORT);
        while(cursor.moveToNext()) {
            Word word = converter.convert(cursor);
            words.add(word);
        }
        cursor.close();

        return words;
    }

}
