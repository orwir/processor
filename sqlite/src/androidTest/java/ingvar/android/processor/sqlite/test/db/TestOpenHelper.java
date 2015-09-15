package ingvar.android.processor.sqlite.test.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Igor Zubenko on 2015.04.22.
 */
public class TestOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sqlite_test";
    private static final int DATABASE_VERSION = 1;

    public TestOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(CREATE_TABLE_TEST);
            db.execSQL(insertIntoTest("test1", 10.20));
            db.execSQL(insertIntoTest("test2", 100.500));

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}


    private static String CREATE_TABLE_TEST = String.format(
        "create table %s (" +
        "%s integer primary key on conflict replace," +
        "%s text not null," +
        "%s real not null," +
        "%s integer default(strftime('%%s', 'now') * 1000) not null" +
        ")",
        TestContract.Test.TABLE_NAME,
        TestContract.Test.Col._ID,
        TestContract.Test.Col.NAME,
        TestContract.Test.Col.PRICE,
        TestContract.Test.Col._CREATION_DATE
    );

    private static String insertIntoTest(String name, Double price) {
        return String.format("insert into %s (%s, %s) values ('%s', '%.3f')",
            TestContract.Test.TABLE_NAME, TestContract.Test.Col.NAME, TestContract.Test.Col.PRICE,
            name, price);
    }

}
