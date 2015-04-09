package ingvar.examples.dictionary.storage;

import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
public class CreationScript {

    public static final class V1 {

        public static final String TABLE_DICTIONARY = createTableDictionary();
        public static final String TABLE_WORDS = createTableWords();
        public static final String TABLE_MEANINGS = createTableMeanings();

        private static final String createTableDictionary() {
            String sql =
            "create table ${table} (" +
                "${id} primary key," +
                "${name} text not null unique," +
                "${creation_date} integer default(strftime('%s', 'now') * 1000) not null" +
            ")";
            Map<String, String> values = new HashMap<>();
            values.put("table", DictionaryContract.Dictionary.TABLE_NAME);
            values.put("id", DictionaryContract.Dictionary.Col._ID);
            values.put("name", DictionaryContract.Dictionary.Col.NAME);
            values.put("creation_date", DictionaryContract.Dictionary.Col._CREATION_DATE);
            return StrSubstitutor.replace(sql, values);
        }

        private static final String createTableWords() {
            String sql =
            "create table ${table} (" +
                "${id} primary key," +
                "${dictionary_id} integer not null references ${dictionary_table}(${dt_id})," +
                "${value} text not null," +
                "${creation_date} integer default(strftime('%s', 'now') * 1000) not null" +
            ")";
            Map<String, String> values = new HashMap<>();
            values.put("table", DictionaryContract.Words.TABLE_NAME);
            values.put("id", DictionaryContract.Words.Col._ID);
            values.put("dictionary_id", DictionaryContract.Words.Col.DICTIONARY_ID);
            values.put("dictionary_table", DictionaryContract.Dictionary.TABLE_NAME);
            values.put("dt_id", DictionaryContract.Dictionary.Col._ID);
            values.put("value", DictionaryContract.Words.Col.VALUE);
            values.put("creation_date", DictionaryContract.Words.Col._CREATION_DATE);
            return StrSubstitutor.replace(sql, values);
        }

        private static final String createTableMeanings() {
            String sql =
            "create table ${table} (" +
                "${id} primary key," +
                "${dictionary_id} integer not null references ${dictionary_table}(${dt_id})," +
                "${word_id} integer not null references ${words_table}(${wt_id})," +
                "${value} text not null," +
                "${creation_date} integer default(strftime('%s', 'now') * 1000) not null" +
            ")";
            Map<String, String> values = new HashMap<>();
            values.put("table", DictionaryContract.Meanings.TABLE_NAME);
            values.put("id", DictionaryContract.Meanings.Col._ID);
            values.put("dictionary_id", DictionaryContract.Meanings.Col.DICTIONARY_ID);
            values.put("dictionary_table", DictionaryContract.Dictionary.TABLE_NAME);
            values.put("dt_id", DictionaryContract.Dictionary.Col._ID);
            values.put("word_id", DictionaryContract.Meanings.Col.WORD_ID);
            values.put("words_table", DictionaryContract.Words.TABLE_NAME);
            values.put("wt_id", DictionaryContract.Words.Col._ID);
            values.put("value", DictionaryContract.Meanings.Col.VALUE);
            values.put("creation_date", DictionaryContract.Meanings.Col._CREATION_DATE);
            return StrSubstitutor.replace(sql, values);
        }

    }

    private CreationScript() {}
}
