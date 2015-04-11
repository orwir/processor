package ingvar.android.processor.examples.dictionary.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.ref.WeakReference;

import ingvar.android.processor.examples.R;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
public class DictionaryHelper extends SQLiteOpenHelper {

    private static final String NAME = "dictionary";
    private static final int VERSION = 1;

    private WeakReference<Context> contextRef;

    public DictionaryHelper(Context context) {
        super(context, NAME, null, VERSION);
        this.contextRef = new WeakReference<>(context);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if(!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(CreationScript.V1.TABLE_DICTIONARY);
            db.execSQL(CreationScript.V1.TABLE_WORDS);
            db.execSQL(CreationScript.V1.TABLE_MEANINGS);

            ContentValues values = new ContentValues();
            values.put(DictionaryContract.Dictionaries.Col.NAME, getContext().getString(R.string.label_default));
            db.insert(DictionaryContract.Dictionaries.TABLE_NAME, null, values);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    protected Context getContext() {
        Context context = contextRef.get();
        if(context == null) {
            throw new IllegalStateException("Context is stale!");
        }
        return context;
    }

}
