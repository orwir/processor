package ingvar.android.processor.examples.dictionary.persistence;

import android.content.ContentValues;
import android.database.Cursor;

import ingvar.android.literepo.conversion.Converter;
import ingvar.android.literepo.util.CursorCommon;
import ingvar.android.processor.examples.dictionary.pojo.Meaning;
import ingvar.android.processor.examples.dictionary.pojo.Word;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;

/**
 * Created by Igor Zubenko on 2015.04.16.
 */
public class MeaningConverter implements Converter<Meaning> {


    private Word word;

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    @Override
    public ContentValues convert(Meaning meaning) {
        ContentValues values = new ContentValues();
        values.put(DictionaryContract.Meanings.Col._ID, meaning.getId());
        values.put(DictionaryContract.Meanings.Col.DICTIONARY_ID, meaning.getWord().getDictionary().getId());
        values.put(DictionaryContract.Meanings.Col.WORD_ID, meaning.getWord().getId());
        values.put(DictionaryContract.Meanings.Col.VALUE, meaning.getValue());

        return values;
    }

    @Override
    public Meaning convert(Cursor cursor) {
        Meaning meaning = new Meaning();
        meaning.setId(CursorCommon.longv(cursor, DictionaryContract.Meanings.Col._ID));
        meaning.setWord(word);
        meaning.setValue(CursorCommon.string(cursor, DictionaryContract.Meanings.Col.VALUE));

        return meaning;
    }

}
