package ingvar.android.processor.examples.dictionary.persistence;

import android.content.ContentValues;
import android.database.Cursor;

import ingvar.android.literepo.conversion.Converter;
import ingvar.android.literepo.util.CursorCommon;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;
import ingvar.android.processor.examples.dictionary.pojo.Word;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;

/**
 * Created by Igor Zubenko on 2015.04.14.
 */
public class WordConverter implements Converter<Word> {

    private Dictionary dictionary;

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public ContentValues convert(Word word) {
        ContentValues values = new ContentValues();
        values.put(DictionaryContract.Words.Col.DICTIONARY_ID, word.getDictionary().getId());
        values.put(DictionaryContract.Words.Col.VALUE, word.getWord());
        return values;
    }

    @Override
    public Word convert(Cursor cursor) {
        Word word = new Word(CursorCommon.string(cursor, DictionaryContract.Words.Col.VALUE));
        word.setId(CursorCommon.longv(cursor, DictionaryContract.Words.Col._ID));
        word.setDictionary(dictionary);
        return word;
    }

}
