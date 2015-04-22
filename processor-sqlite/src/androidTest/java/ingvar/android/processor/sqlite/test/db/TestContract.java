package ingvar.android.processor.sqlite.test.db;

import android.net.Uri;

import ingvar.android.processor.sqlite.persistence.ExtendedColumns;

/**
 * Created by Igor Zubenko on 2015.04.22.
 */
public class TestContract {

    public static final String AUTHORITY = "ingvar.android.processor.sqlite.test.provider";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY);

    public static class Test {

        public static final String TABLE_NAME = "test";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(URI, TABLE_NAME);
        public static final String[] PROJECTION = {Col._ID, Col.NAME, Col.PRICE, Col._CREATION_DATE};

        public static class Col implements ExtendedColumns {
            public static final String NAME = "name";
            public static final String PRICE = "price";
        }

    }

    private TestContract() {}
}
